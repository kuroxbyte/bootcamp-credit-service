package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.application.rule.OverdueDebtRule;
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
    private final OverdueDebtRule overdueDebtRule;
    private final CreditEventPublisher creditEventPublisher;
    private final Clock clock;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos, la regla de deuda vencida y el
     * reloj que fija la fecha de otorgamiento.
     */
    public CreateCreditCardUseCaseImpl(
            CreditCardRepository creditCardRepository,
            CustomerClient customerClient,
            OverdueDebtRule overdueDebtRule,
            CreditEventPublisher creditEventPublisher,
            Clock clock) {
        this.creditCardRepository = creditCardRepository;
        this.customerClient = customerClient;
        this.overdueDebtRule = overdueDebtRule;
        this.creditEventPublisher = creditEventPublisher;
        this.clock = clock;
    }

    @Override
    public Single<CreditCard> execute(
            String customerId,
            BigDecimal creditLimit,
            int paymentDueDay) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .flatMap(customer -> overdueDebtRule.verify(customerId)
                        .andThen(Single.fromCallable(
                                () -> new CreditCard(customer, creditLimit, paymentDueDay))))
                .flatMap(creditCard -> {
                    creditCard.setGrantedAt(LocalDate.now(clock));
                    return creditCardRepository.save(creditCard);
                })
                .flatMap(saved -> creditEventPublisher
                        .publishCreditCardGranted(saved)
                        .andThen(Single.just(saved)));
    }
}
