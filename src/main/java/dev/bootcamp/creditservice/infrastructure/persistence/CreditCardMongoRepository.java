package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link CreditCardDocument}.
 */
public interface CreditCardMongoRepository
        extends ReactiveMongoRepository<CreditCardDocument, String> {
    /**
     * Busca las tarjetas activas (sin fecha de baja) del cliente.
     */
    Flux<CreditCardDocument> findByCustomerIdAndClosedAtIsNull(String customerId);
}
