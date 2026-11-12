package dev.bootcamp.creditservice.infrastructure.messaging;

import dev.bootcamp.creditservice.application.usecase.UpdateCustomerDebtStatusUseCase;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Consume el tópico {@code debt-events} de debt-service y guarda el estado de deuda de cada
 * cliente en la copia local. El contrato está en {@code docs/events/asyncapi.yaml} del repositorio
 * raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si guardar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebtEventListener {

    static final String CUSTOMER_DEBT_STATUS_CHANGED = "CustomerDebtStatusChanged";

    private final UpdateCustomerDebtStatusUseCase updateCustomerDebtStatusUseCase;
    private final JsonMapper jsonMapper;

    /**
     * Procesa un evento del tópico de deudas.
     *
     * @param message evento en JSON
     */
    @KafkaListener(topics = "${messaging.topics.debt-events}")
    public void onDebtEvent(String message) {
        JsonNode event = jsonMapper.readTree(message);
        String eventType = event.path("eventType").asString();
        if (!CUSTOMER_DEBT_STATUS_CHANGED.equals(eventType)) {
            log.debug("Ignoring debt event type={}", eventType);
            return;
        }
        CustomerDebtStatus status = new CustomerDebtStatus(
                event.get("customerId").asString(),
                event.get("hasOverdueDebt").asBoolean(),
                event.get("overdueAmount").decimalValue(),
                Instant.parse(event.get("occurredAt").asString()));
        updateCustomerDebtStatusUseCase.execute(status).blockingAwait();
        log.info(
                "Debt status updated customerId={} hasOverdueDebt={}",
                status.customerId(),
                status.hasOverdueDebt());
    }
}
