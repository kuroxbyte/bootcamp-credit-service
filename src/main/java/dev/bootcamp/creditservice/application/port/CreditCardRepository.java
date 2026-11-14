package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de las tarjetas de crédito.
 */
public interface CreditCardRepository {

    /**
     * Busca la tarjeta por su id, aunque esté dada de baja.
     *
     * @return la tarjeta, o vacío si no existe
     */
    Maybe<CreditCard> findById(String creditCardId);

    /**
     * Busca los productos del cliente.
     *
     * @return las tarjetas activas del cliente (ninguna si no tiene)
     */
    Flowable<CreditCard> findByCustomerId(String customerId);

    /**
     * Guarda la tarjeta y la emite con su id asignado.
     */
    Single<CreditCard> save(CreditCard creditCard);

    /**
     * Todas las tarjetas de crédito del banco, también las dadas de baja.
     */
    Flowable<CreditCard> findAll();
}
