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

    /**
     * Publica que cambió la fecha de vencimiento de un crédito.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditUpdated(Credit credit);

    /**
     * Publica que cambiaron el límite o el día de pago de una tarjeta.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditCardUpdated(CreditCard creditCard);

    /**
     * Publica que se dio de baja un crédito.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditClosed(Credit credit);

    /**
     * Publica que se dio de baja una tarjeta de crédito.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCreditCardClosed(CreditCard creditCard);
}
