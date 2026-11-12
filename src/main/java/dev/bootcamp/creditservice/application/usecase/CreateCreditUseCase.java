package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Caso de uso para otorgar un crédito personal o empresarial a un cliente.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>El cliente debe existir en customer-service.
 *   <li>Crédito personal: solo para clientes personales y máximo uno por persona.
 *   <li>Crédito empresarial: solo para clientes empresariales, sin límite por empresa.
 *   <li>No se exige que el cliente tenga una cuenta bancaria en la institución.
 *   <li>El cliente no puede tener deuda vencida en ningún producto de crédito.
 *   <li>El monto debe ser mayor a 0 y la fecha de vencimiento, posterior a hoy.
 *   <li>Al otorgarlo publica el evento {@code CreditGranted}.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * NotFoundException} si el cliente o el producto no existe (o no pertenece al cliente), y con error
 * {@link BusinessRuleException} si se incumple una regla de negocio; en ambos casos no se guarda
 * nada.
 */
public interface CreateCreditUseCase {

    /**
     * Crea y guarda un crédito para el cliente indicado.
     *
     * @param customerId id del cliente en customer-service
     * @param type tipo de crédito a otorgar
     * @param amount monto otorgado
     * @param dueDate fecha de vencimiento; debe ser posterior a hoy
     * @return el crédito guardado; error {@link NotFoundException} si el cliente no existe; {@link
     *     BusinessRuleException} si el cliente tiene deuda vencida, el tipo de crédito no
     *     corresponde al tipo de cliente, el cliente personal ya tiene un crédito personal, el
     *     monto no es mayor a 0 o la fecha de vencimiento no es posterior a hoy
     */
    Single<Credit> execute(
            String customerId,
            CreditType type,
            BigDecimal amount,
            LocalDate dueDate);
}
