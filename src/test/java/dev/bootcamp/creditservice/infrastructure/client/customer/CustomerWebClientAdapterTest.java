package dev.bootcamp.creditservice.infrastructure.client.customer;

import static org.assertj.core.api.Assertions.assertThat;

import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import dev.bootcamp.creditservice.infrastructure.client.ServiceUnavailableException;
import dev.bootcamp.creditservice.infrastructure.client.StubWebClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CustomerWebClientAdapterTest {

    private static CustomerWebClientAdapter adapter(StubWebClient stub, Duration timeout) {
        return new CustomerWebClientAdapter(stub.builder(), "http://customer-service", timeout);
    }

    @Test
    void returnsCustomerFromCustomerService() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.OK,
                "{\"id\":\"1\",\"name\":\"Ana Torres\",\"type\":\"PERSONAL\"}");

        adapter(stub, Duration.ofSeconds(1)).findById("1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertValue(new Customer("1", CustomerType.PERSONAL));
        assertThat(stub.lastPath()).isEqualTo("/api/v1/customers/1");
    }

    @Test
    void returnsEmptyWhenCustomerDoesNotExist() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.NOT_FOUND, "{\"status\":404}");

        adapter(stub, Duration.ofSeconds(1)).findById("99").test().awaitDone(2, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
    }

    @Test
    void failsAsUnavailableOnServerError() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.INTERNAL_SERVER_ERROR, "{\"status\":500}");

        adapter(stub, Duration.ofSeconds(1)).findById("1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }

    @Test
    void failsAsUnavailableOnConnectionError() {
        StubWebClient stub = StubWebClient.failingWith(new IllegalStateException("Connection refused"));

        adapter(stub, Duration.ofSeconds(1)).findById("1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }

    @Test
    void failsAsUnavailableOnTimeout() {
        adapter(StubWebClient.neverResponding(), Duration.ofMillis(100)).findById("1").test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }
}
