package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

/**
 * Caso de uso para otorgar una tarjeta de crédito a un cliente.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El cliente debe existir en customer-service.</li>
 *   <li>Se otorga a clientes personales o empresariales.</li>
 *   <li>El límite de crédito debe ser mayor a 0; define cuánto puede consumir el cliente.</li>
 *   <li>No se exige que el cliente tenga una cuenta bancaria en la institución.</li>
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Si se incumple una
 * regla, el {@link Single} termina con error {@link BusinessRuleException} y la tarjeta no se guarda.
 */
public interface CreateCreditCardUseCase {

    /**
     * Crea y guarda una tarjeta de crédito para el cliente indicado.
     *
     * @param customerId  id del cliente en customer-service
     * @param creditLimit límite de crédito de la tarjeta
     * @return la tarjeta guardada; error {@link BusinessRuleException} si el cliente no existe
     *         o el límite de crédito no es mayor a 0
     */
    Single<CreditCard> execute(String customerId, BigDecimal creditLimit);
}
