package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;

/**
 * Implementación de {@link UpdateCustomerDebtStatusUseCase}.
 */
public class UpdateCustomerDebtStatusUseCaseImpl implements UpdateCustomerDebtStatusUseCase {
    private final CustomerDebtStatusRepository customerDebtStatusRepository;

    /**
     * Crea el caso de uso con el adaptador de su puerto.
     */
    public UpdateCustomerDebtStatusUseCaseImpl(
            CustomerDebtStatusRepository customerDebtStatusRepository) {
        this.customerDebtStatusRepository = customerDebtStatusRepository;
    }

    @Override
    public Completable execute(CustomerDebtStatus status) {
        return customerDebtStatusRepository.save(status);
    }
}
