package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CreditDebtClient;
import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link UpdateCreditCardUseCase}.
 */
@RequiredArgsConstructor
public class UpdateCreditCardUseCaseImpl implements UpdateCreditCardUseCase {
    private final CreditCardRepository creditCardRepository;
    private final CreditDebtClient creditDebtClient;
    private final CreditEventPublisher creditEventPublisher;
    private final Clock clock;

    @Override
    public Single<CreditCard> update(
            String creditCardId,
            BigDecimal creditLimit,
            int paymentDueDay) {
        return GetCreditCardsUseCaseImpl.findActive(creditCardRepository, creditCardId)
                .flatMap(card -> {
                    CreditCard updated = card.withTerms(creditLimit, paymentDueDay);
                    return debt(card).flatMap(debt -> creditLimit.compareTo(debt) < 0
                            ? Single.<CreditCard>error(new BusinessRuleException(
                                    "El límite no puede ser menor que la deuda actual de "
                                            + debt.toPlainString()))
                            : Single.just(updated));
                })
                .flatMap(creditCardRepository::save)
                .flatMap(saved -> creditEventPublisher.publishCreditCardUpdated(saved)
                        .andThen(Single.just(saved)));
    }

    @Override
    public Completable close(String creditCardId) {
        return GetCreditCardsUseCaseImpl.findActive(creditCardRepository, creditCardId)
                .flatMap(card -> debt(card).flatMap(debt -> {
                    if (debt.signum() > 0) {
                        return Single.<CreditCard>error(new BusinessRuleException(
                                "La tarjeta tiene una deuda de " + debt.toPlainString()
                                        + "; debe pagarse por completo para darla de baja"));
                    }
                    card.close(LocalDate.now(clock));
                    return creditCardRepository.save(card);
                }))
                .flatMapCompletable(creditEventPublisher::publishCreditCardClosed);
    }

    // Deuda de la tarjeta: su límite actual menos el crédito disponible.
    private Single<BigDecimal> debt(CreditCard card) {
        return creditDebtClient.availableCredit(card.getId(), card.getCustomer().id())
                .map(available -> card.getCreditLimit().subtract(available).max(BigDecimal.ZERO));
    }
}
