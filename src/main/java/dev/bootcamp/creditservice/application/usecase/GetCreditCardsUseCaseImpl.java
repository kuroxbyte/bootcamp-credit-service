package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link GetCreditCardsUseCase}.
 */
@RequiredArgsConstructor
public class GetCreditCardsUseCaseImpl implements GetCreditCardsUseCase {
    private final CreditCardRepository creditCardRepository;

    @Override
    public Flowable<CreditCard> findByCustomerId(String customerId) {
        return creditCardRepository.findByCustomerId(customerId);
    }

    @Override
    public Single<CreditCard> findById(String id) {
        return findActive(creditCardRepository, id);
    }

    @Override
    public Flowable<CreditCard> findAll() {
        return creditCardRepository.findAll();
    }

    /**
     * Busca una tarjeta activa.
     *
     * @return la tarjeta; error {@link NotFoundException} si no existe o fue dada de baja
     */
    static Single<CreditCard> findActive(
            CreditCardRepository creditCardRepository,
            String creditCardId) {
        return creditCardRepository
                .findById(creditCardId)
                .filter(card -> !card.isClosed())
                .switchIfEmpty(Single.error(
                        () -> new NotFoundException("La tarjeta de crédito no existe")));
    }
}
