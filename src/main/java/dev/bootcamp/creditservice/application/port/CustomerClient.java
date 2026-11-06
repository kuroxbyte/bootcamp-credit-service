package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.customer.Customer;
import java.util.Optional;

/** Consulta de clientes en customer-service. */
public interface CustomerClient {
    Optional<Customer> findById(String customerId);
}
