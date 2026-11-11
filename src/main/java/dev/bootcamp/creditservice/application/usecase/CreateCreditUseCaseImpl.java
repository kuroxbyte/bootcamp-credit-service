package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Single;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link CreateCreditUseCase}.
 */
public class CreateCreditUseCaseImpl implements CreateCreditUseCase {
    private final CreditRepository creditRepository;
    private final CustomerClient customerClient;
    private final Clock clock;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos y el reloj que fija la fecha de
     * otorgamiento.
     */
    public CreateCreditUseCaseImpl(
            CreditRepository creditRepository,
            CustomerClient customerClient,
            Clock clock) {
        this.creditRepository = creditRepository;
        this.customerClient = customerClient;
        this.clock = clock;
    }

    @Override
    public Single<Credit> execute(String customerId, CreditType type) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .map(customer -> new Credit(customer, type))
                .flatMap(this::validateOnePersonalCreditPerCustomer)
                .flatMap(credit -> {
                    credit.setGrantedAt(LocalDate.now(clock));
                    return creditRepository.save(credit);
                });
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
