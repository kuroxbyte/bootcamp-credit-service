package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Caso de uso para otorgar una tarjeta de crédito a un cliente.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>El cliente debe existir en customer-service.
 *   <li>Se otorga a clientes personales o empresariales.
 *   <li>El límite de crédito debe ser mayor a 0; define cuánto puede consumir el cliente.
 *   <li>No se exige que el cliente tenga una cuenta bancaria en la institución.
 *   <li>El cliente no puede tener deuda vencida en ningún producto de crédito.
 *   <li>El día de pago está entre 1 y 28: los consumos de un mes se pagan hasta ese día del mes
 *       siguiente.
 *   <li>Al otorgarla publica el evento {@code CreditCardGranted}.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * NotFoundException} si el cliente o el producto no existe (o no pertenece al cliente), y con error
 * {@link BusinessRuleException} si se incumple una regla de negocio; en ambos casos no se guarda
 * nada.
 */
public interface CreateCreditCardUseCase {

    /**
     * Crea y guarda una tarjeta de crédito para el cliente indicado.
     *
     * @param customerId id del cliente en customer-service
     * @param creditLimit límite de crédito de la tarjeta
     * @param paymentDueDay día de pago mensual (1 a 28)
     * @return la tarjeta guardada; error {@link NotFoundException} si el cliente no existe; {@link
     *     BusinessRuleException} si el cliente tiene deuda vencida, el límite de crédito no es
     *     mayor a 0 o el día de pago no está entre 1 y 28
     */
    Single<CreditCard> execute(String customerId, BigDecimal creditLimit, int paymentDueDay);
}
