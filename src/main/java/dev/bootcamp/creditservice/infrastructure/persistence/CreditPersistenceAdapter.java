package dev.bootcamp.creditservice.infrastructure.persistence;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto {@link CreditRepository} con MongoDB reactivo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditPersistenceAdapter implements CreditRepository {

    private final CreditMongoRepository mongoRepository;

    @Override
    public Single<Boolean> existsByCustomerIdAndType(String customerId, CreditType type) {
        return Single.fromPublisher(
                mongoRepository.existsByCustomerIdAndType(customerId, type.name()));
    }

    @Override
    public Maybe<Credit> findById(String creditId) {
        return Maybe.fromPublisher(mongoRepository.findById(creditId))
                .map(CreditPersistenceMapper::toDomain);
    }

    @Override
    public Flowable<Credit> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(mongoRepository.findByCustomerId(customerId))
                .map(CreditPersistenceMapper::toDomain);
    }

    @Override
    public Single<Credit> save(Credit credit) {
        return Single.fromPublisher(
                        mongoRepository.save(CreditPersistenceMapper.toDocument(credit)))
                .doOnSuccess(saved -> log.debug("Credit saved creditId={}", saved.getId()))
                .map(CreditPersistenceMapper::toDomain);
    }
}
