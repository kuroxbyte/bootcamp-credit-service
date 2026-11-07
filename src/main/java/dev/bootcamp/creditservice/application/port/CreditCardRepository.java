package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface CreditCardRepository {

    /**
     * @return la tarjeta, o vacío si no existe
     */
    Maybe<CreditCard> findById(String creditCardId);

    /**
     * @return las tarjetas del cliente (ninguna si no tiene)
     */
    Flowable<CreditCard> findByCustomerId(String customerId);

    Single<CreditCard> save(CreditCard creditCard);
}
