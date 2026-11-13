package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link GetCreditsUseCase}.
 */
@RequiredArgsConstructor
public class GetCreditsUseCaseImpl implements GetCreditsUseCase {
    private final CreditRepository creditRepository;

    @Override
    public Flowable<Credit> findByCustomerId(String customerId) {
        return creditRepository.findByCustomerId(customerId);
    }

    @Override
    public Single<Credit> findById(String id) {
        return creditRepository
                .findById(id)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El crédito no existe")));
    }

    @Override
    public Flowable<Credit> findAll() {
        return creditRepository.findAll();
    }
}
