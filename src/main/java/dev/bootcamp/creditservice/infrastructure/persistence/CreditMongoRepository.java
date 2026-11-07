package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditMongoRepository extends ReactiveMongoRepository<CreditDocument, String> {
    Flux<CreditDocument> findByCustomerId(String customerId);

    Mono<Boolean> existsByCustomerIdAndType(String customerId, String type);
}
