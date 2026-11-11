package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link CreateCreditCardUseCase}.
 */
public class CreateCreditCardUseCaseImpl implements CreateCreditCardUseCase {
    private final CreditCardRepository creditCardRepository;
    private final CustomerClient customerClient;
    private final Clock clock;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos y el reloj que fija la fecha de
     * otorgamiento.
     */
    public CreateCreditCardUseCaseImpl(
            CreditCardRepository creditCardRepository,
            CustomerClient customerClient,
            Clock clock) {
        this.creditCardRepository = creditCardRepository;
        this.customerClient = customerClient;
        this.clock = clock;
    }

    @Override
    public Single<CreditCard> execute(String customerId, BigDecimal creditLimit) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .map(customer -> new CreditCard(customer, creditLimit))
                .flatMap(creditCard -> {
                    creditCard.setGrantedAt(LocalDate.now(clock));
                    return creditCardRepository.save(creditCard);
                });
    }
}
