package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link CreateCreditUseCase}.
 */
public class CreateCreditUseCaseImpl implements CreateCreditUseCase {
    private final CreditRepository creditRepository;
    private final CustomerClient customerClient;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos.
     */
    public CreateCreditUseCaseImpl(
            CreditRepository creditRepository,
            CustomerClient customerClient) {
        this.creditRepository = creditRepository;
        this.customerClient = customerClient;
    }

    @Override
    public Single<Credit> execute(String customerId, CreditType type) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .map(customer -> new Credit(customer, type))
                .flatMap(this::validateOnePersonalCreditPerCustomer)
                .flatMap(creditRepository::save);
    }

    // Personal: solo un crédito por persona. Empresarial: sin límite.
    private Single<Credit> validateOnePersonalCreditPerCustomer(Credit credit) {
        if (credit.getType() != CreditType.PERSONAL) {
            return Single.just(credit);
        }
        return creditRepository
                .existsByCustomerIdAndType(credit.getCustomer().id(), CreditType.PERSONAL)
                .flatMap(
                        exists ->
                                exists
                                        ? Single.error(
                                                new BusinessRuleException(
                                                        "El cliente ya tiene un crédito personal"))
                                        : Single.just(credit));
    }
}
