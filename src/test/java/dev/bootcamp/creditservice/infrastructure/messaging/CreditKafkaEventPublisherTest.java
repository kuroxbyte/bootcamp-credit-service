package dev.bootcamp.creditservice.infrastructure.messaging;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditKafkaEventPublisherTest {

    private static final Customer CUSTOMER =
            new Customer("c1", CustomerType.BUSINESS, CustomerProfile.STANDARD);

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private CreditKafkaEventPublisher publisher;

    @BeforeEach
    void createPublisher() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        publisher = new CreditKafkaEventPublisher(
                kafkaTemplate,
                jsonMapper,
                clock,
                "credit-events");
    }

    private JsonNode sentEvent() {
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("credit-events"), eq("c1"), json.capture());
        return jsonMapper.readTree(json.getValue());
    }

    @Test
    void publishesCreditGrantedWithCustomerAsKey() {
        when(kafkaTemplate.send(eq("credit-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        Credit credit = new Credit(
                CUSTOMER,
                CreditType.BUSINESS,
                new BigDecimal("5000"),
                LocalDate.of(2027, 1, 15));
        credit.setId("k1");
        credit.grant(LocalDate.of(2026, 11, 15));

        publisher.publishCreditGranted(credit).test().awaitDone(
                2,
                TimeUnit.SECONDS).assertComplete();

        JsonNode event = sentEvent();
        assertThat(event.get("eventType").asString()).isEqualTo("CreditGranted");
        assertThat(event.get("creditId").asString()).isEqualTo("k1");
        assertThat(event.get("creditType").asString()).isEqualTo("BUSINESS");
        assertThat(event.get("amount").decimalValue()).isEqualByComparingTo("5000");
        assertThat(event.get("dueDate").asString()).isEqualTo("2027-01-15");
        assertThat(event.get("grantedAt").asString()).isEqualTo("2026-11-15");
        assertThat(event.get("occurredAt").asString()).isEqualTo("2026-11-15T10:00:00Z");
        assertThat(event.get("eventId").asString()).hasSize(36);
    }

    @Test
    void publishesCreditCardGrantedWithPaymentDueDay() {
        when(kafkaTemplate.send(eq("credit-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        CreditCard card = new CreditCard(CUSTOMER, new BigDecimal("1000"), 15);
        card.setId("t1");
        card.setGrantedAt(LocalDate.of(2026, 11, 15));

        publisher.publishCreditCardGranted(card).test().awaitDone(
                2,
                TimeUnit.SECONDS).assertComplete();

        JsonNode event = sentEvent();
        assertThat(event.get("eventType").asString()).isEqualTo("CreditCardGranted");
        assertThat(event.get("creditCardId").asString()).isEqualTo("t1");
        assertThat(event.get("paymentDueDay").asInt()).isEqualTo(15);
    }

    @Test
    void doesNotFailWhenKafkaIsUnavailable() {
        when(kafkaTemplate.send(eq("credit-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));
        CreditCard card = new CreditCard(CUSTOMER, new BigDecimal("1000"), 15);
        card.setGrantedAt(LocalDate.of(2026, 11, 15));

        publisher.publishCreditCardGranted(card).test().awaitDone(
                2,
                TimeUnit.SECONDS).assertComplete();
    }
}
