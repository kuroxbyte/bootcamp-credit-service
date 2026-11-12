package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Repositorio de Spring Data para {@link CustomerDebtStatusDocument}.
 */
public interface CustomerDebtStatusMongoRepository
        extends ReactiveMongoRepository<CustomerDebtStatusDocument, String> {}
