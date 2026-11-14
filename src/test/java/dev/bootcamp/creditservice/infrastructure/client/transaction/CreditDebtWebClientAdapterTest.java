package dev.bootcamp.creditservice.infrastructure.client.transaction;

import dev.bootcamp.creditservice.infrastructure.client.ServiceUnavailableException;
import dev.bootcamp.creditservice.infrastructure.client.StubWebClient;
import dev.bootcamp.creditservice.infrastructure.client.TestCircuitBreakers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CreditDebtWebClientAdapterTest {

    private static CreditDebtWebClientAdapter adapter(StubWebClient stub) {
        return new CreditDebtWebClientAdapter(
                stub.builder(),
                "http://transaction-service",
                TestCircuitBreakers.remoteLookup(Duration.ofSeconds(1)));
    }

    @Test
    void addsOnlyThePaymentsOfTheCredit() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.OK,
                "[{\"type\":\"PAYMENT\",\"amount\":100,\"date\":\"2026-11-20\"},"
                        + "{\"type\":\"PAYMENT\",\"amount\":50.5,\"date\":\"2026-12-20\"}]");

        BigDecimal paid = adapter(stub).paidAmount("k1", "c1").blockingGet();

        assertThat(paid).isEqualByComparingTo("150.5");
        assertThat(stub.lastPath()).isEqualTo("/api/v1/credits/k1/movements");
    }

    @Test
    void readsTheAvailableCreditOfTheCard() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.OK,
                "{\"creditCardId\":\"t1\",\"availableCredit\":4200}");

        assertThat(adapter(stub).availableCredit("t1", "c1").blockingGet())
                .isEqualByComparingTo("4200");
        assertThat(stub.lastPath()).isEqualTo("/api/v1/credit-cards/t1/available-credit");
    }

    @Test
    void failsAsUnavailableWhenTransactionServiceFailsOrDoesNotKnowTheProduct() {
        adapter(StubWebClient.respondingWith(HttpStatus.NOT_FOUND, "{}"))
                .availableCredit("t1", "c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
        adapter(StubWebClient.respondingWith(HttpStatus.INTERNAL_SERVER_ERROR, "{}"))
                .paidAmount("k1", "c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }
}
