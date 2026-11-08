package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/** Repositorio de Spring Data para {@link CreditCardDocument}. */
public interface CreditCardMongoRepository
        extends ReactiveMongoRepository<CreditCardDocument, String> {
    /** Busca los productos del cliente. */
    Flux<CreditCardDocument> findByCustomerId(String customerId);
}
