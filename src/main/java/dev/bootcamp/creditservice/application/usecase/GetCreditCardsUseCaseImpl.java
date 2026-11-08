package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link GetCreditCardsUseCase}.
 */
public class GetCreditCardsUseCaseImpl implements GetCreditCardsUseCase {
    private final CreditCardRepository creditCardRepository;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos.
     */
    public GetCreditCardsUseCaseImpl(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    @Override
    public Flowable<CreditCard> findByCustomerId(String customerId) {
        return creditCardRepository.findByCustomerId(customerId);
    }

    @Override
    public Single<CreditCard> findById(String id) {
        return creditCardRepository
                .findById(id)
                .switchIfEmpty(
                        Single.error(
                                () -> new NotFoundException("La tarjeta de crédito no existe")));
    }
}
