package dev.bootcamp.creditservice.infrastructure.client.customer;

import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import dev.bootcamp.creditservice.infrastructure.client.RemoteLookup;
import dev.bootcamp.creditservice.infrastructure.client.customer.dto.CustomerResponseDto;
import io.reactivex.rxjava3.core.Maybe;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** Implementa el puerto {@link CustomerClient} consultando customer-service con WebClient. */
@Component
public class CustomerWebClientAdapter implements CustomerClient {

    private static final String SERVICE_NAME = "customer-service";

    private final WebClient webClient;
    private final Duration timeout;

    /** Crea el cliente con la URL base y el tiempo de espera configurados. */
    public CustomerWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${clients.customer-service.base-url}") String baseUrl,
            @Value("${clients.customer-service.timeout}") Duration timeout) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = timeout;
    }

    @Override
    public Maybe<Customer> findById(String customerId) {
        return RemoteLookup.findById(
                webClient
                        .get()
                        .uri("/api/v1/customers/{customerId}", customerId)
                        .retrieve()
                        .bodyToMono(CustomerResponseDto.class)
                        .map(CustomerWebClientAdapter::toDomain),
                timeout,
                SERVICE_NAME,
                customerId);
    }

    private static Customer toDomain(CustomerResponseDto response) {
        return new Customer(response.getId(), CustomerType.valueOf(response.getType().name()));
    }
}
