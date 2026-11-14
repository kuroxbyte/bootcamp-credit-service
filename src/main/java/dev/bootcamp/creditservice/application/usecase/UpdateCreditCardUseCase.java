package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Casos de uso del empleado del banco sobre una tarjeta de crédito: cambiar su límite y su día de
 * pago, y darla de baja. Publican {@code CreditCardUpdated} y {@code CreditCardClosed}.
 *
 * <p>Son reactivos: no hacen nada hasta que se suscribe.
 */
public interface UpdateCreditCardUseCase {

    /**
     * Cambia el límite de crédito y el día de pago. El nuevo límite no puede ser menor que lo que
     * ya se debe de la tarjeta.
     *
     * @return la tarjeta modificada; error {@link NotFoundException} si no existe o fue dada de
     *     baja, o {@link BusinessRuleException} si los valores no son válidos
     */
    Single<CreditCard> update(String creditCardId, BigDecimal creditLimit, int paymentDueDay);

    /**
     * Da de baja la tarjeta (baja lógica). Solo si no tiene deuda (disponible igual al límite).
     *
     * @return termina sin error si se dio de baja; error {@link NotFoundException} si no existe o
     *     ya fue dada de baja, o {@link BusinessRuleException} si tiene deuda
     */
    Completable close(String creditCardId);
}
