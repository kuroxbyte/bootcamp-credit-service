package dev.bootcamp.creditservice.infrastructure.persistence;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Implementa el puerto {@link CreditCardRepository} con MongoDB reactivo. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditCardPersistenceAdapter implements CreditCardRepository {

    private final CreditCardMongoRepository mongoRepository;

    @Override
    public Maybe<CreditCard> findById(String creditCardId) {
        return Maybe.fromPublisher(mongoRepository.findById(creditCardId))
                .map(CreditPersistenceMapper::toDomain);
    }

    @Override
    public Flowable<CreditCard> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(mongoRepository.findByCustomerId(customerId))
                .map(CreditPersistenceMapper::toDomain);
    }

    @Override
    public Single<CreditCard> save(CreditCard creditCard) {
        return Single.fromPublisher(mongoRepository.save(CreditPersistenceMapper.toDocument(creditCard)))
                .doOnSuccess(saved -> log.debug("Credit card saved creditCardId={}", saved.getId()))
                .map(CreditPersistenceMapper::toDomain);
    }
}
