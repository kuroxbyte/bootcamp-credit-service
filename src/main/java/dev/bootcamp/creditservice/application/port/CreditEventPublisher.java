package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Completable;

/**
 * Publicación de eventos de productos de crédito para otros microservicios.
 */
public interface CreditEventPublisher {

    /**
     * Publica que se otorgó un crédito.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditGranted(Credit credit);

    /**
     * Publica que se otorgó una tarjeta de crédito.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditCardGranted(CreditCard creditCard);
}
