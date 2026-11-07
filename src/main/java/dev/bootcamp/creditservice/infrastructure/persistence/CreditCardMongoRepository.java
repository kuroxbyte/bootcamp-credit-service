package dev.bootcamp.creditservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CreditCardMongoRepository extends ReactiveMongoRepository<CreditCardDocument, String> {
    Flux<CreditCardDocument> findByCustomerId(String customerId);
}
