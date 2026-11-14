package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio de Spring Data para {@link CreditDocument}.
 */
public interface CreditMongoRepository extends ReactiveMongoRepository<CreditDocument, String> {
    /**
     * Busca los créditos activos (sin fecha de baja) del cliente.
     */
    Flux<CreditDocument> findByCustomerIdAndClosedAtIsNull(String customerId);

    /**
     * Indica si el cliente ya tiene un crédito activo del tipo indicado.
     */
    Mono<Boolean> existsByCustomerIdAndTypeAndClosedAtIsNull(String customerId, String type);
}
