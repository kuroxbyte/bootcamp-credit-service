package dev.bootcamp.creditservice.infrastructure.messaging;

import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementa el puerto {@link CreditEventPublisher} publicando en el tópico {@code credit-events}.
 * La clave de cada mensaje es el id del cliente, para que sus eventos lleguen en orden. El
 * contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>Publica el otorgamiento, la modificación y la baja de créditos y tarjetas, siempre con el
 * estado completo del producto. Si Kafka no responde ({@code max.block.ms}, 1 s en config-repo),
 * el error se registra y la operación no falla: el evento se pierde (limitación conocida;
 * evitarla requiere el patrón outbox).
 */
@Slf4j
@Component
public class CreditKafkaEventPublisher implements CreditEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final String topic;

    /**
     * Crea el publicador con el tópico configurado.
     */
    public CreditKafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            Clock clock,
            @Value("${messaging.topics.credit-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
        this.topic = topic;
    }

    @Override
    public Completable publishCreditGranted(Credit credit) {
        return publishCredit("CreditGranted", credit);
    }

    @Override
    public Completable publishCreditUpdated(Credit credit) {
        return publishCredit("CreditUpdated", credit);
    }

    @Override
    public Completable publishCreditClosed(Credit credit) {
        return publishCredit("CreditClosed", credit);
    }

    @Override
    public Completable publishCreditCardGranted(CreditCard creditCard) {
        return publishCreditCard("CreditCardGranted", creditCard);
    }

    @Override
    public Completable publishCreditCardUpdated(CreditCard creditCard) {
        return publishCreditCard("CreditCardUpdated", creditCard);
    }

    @Override
    public Completable publishCreditCardClosed(CreditCard creditCard) {
        return publishCreditCard("CreditCardClosed", creditCard);
    }

    // Evento con el estado completo del crédito.
    private Completable publishCredit(String eventType, Credit credit) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", eventType);
        event.put("creditId", credit.getId());
        event.put("customerId", credit.getCustomer().id());
        event.put("creditType", credit.getType().name());
        event.put("amount", credit.getAmount());
        event.put("dueDate", credit.getDueDate().toString());
        event.put("grantedAt", credit.getGrantedAt().toString());
        return publish(credit.getCustomer().id(), event);
    }

    // Evento con el estado completo de la tarjeta.
    private Completable publishCreditCard(String eventType, CreditCard creditCard) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", eventType);
        event.put("creditCardId", creditCard.getId());
        event.put("customerId", creditCard.getCustomer().id());
        event.put("creditLimit", creditCard.getCreditLimit());
        event.put("paymentDueDay", creditCard.getPaymentDueDay());
        event.put("grantedAt", creditCard.getGrantedAt().toString());
        return publish(creditCard.getCustomer().id(), event);
    }

    private Completable publish(String customerId, Map<String, Object> event) {
        return Completable.defer(() -> {
            // Id único: debt-service lo usa para no procesar dos veces el mismo evento.
            event.put("eventId", UUID.randomUUID().toString());
            event.put("occurredAt", Instant.now(clock).toString());
            return Completable.fromCompletionStage(kafkaTemplate.send(
                    topic,
                    customerId,
                    jsonMapper.writeValueAsString(event)));
        })
                // send() puede bloquear mientras busca al broker: fuera del hilo de eventos.
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log.info(
                        "{} published customerId={}",
                        event.get("eventType"),
                        customerId))
                .onErrorComplete(error -> {
                    log.error(
                            "{} could not be published customerId={}",
                            event.get("eventType"),
                            customerId,
                            error);
                    return true;
                });
    }
}
