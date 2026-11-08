package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Maybe;

/** Consulta de clientes en customer-service. */
public interface CustomerClient {

    /**
     * Busca el recurso por su id.
     *
     * @return el cliente, o vacío si no existe
     */
    Maybe<Customer> findById(String customerId);
}
