package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Repositorio de Spring Data para {@link CreditDocument}. */
public interface CreditMongoRepository extends ReactiveMongoRepository<CreditDocument, String> {
    /** Busca los productos del cliente. */
    Flux<CreditDocument> findByCustomerId(String customerId);

    /** Indica si el cliente ya tiene un crédito del tipo indicado. */
    Mono<Boolean> existsByCustomerIdAndType(String customerId, String type);
}
