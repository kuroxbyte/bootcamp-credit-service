package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link UpdateCustomerDebtStatusUseCase}.
 */
@RequiredArgsConstructor
public class UpdateCustomerDebtStatusUseCaseImpl implements UpdateCustomerDebtStatusUseCase {
    private final CustomerDebtStatusRepository customerDebtStatusRepository;

    @Override
    public Completable execute(CustomerDebtStatus status) {
        return customerDebtStatusRepository.save(status);
    }
}
