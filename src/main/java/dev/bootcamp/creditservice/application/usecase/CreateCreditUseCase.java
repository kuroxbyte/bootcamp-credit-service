package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;

/**
 * Caso de uso para otorgar un crédito personal o empresarial a un cliente.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El cliente debe existir en customer-service.</li>
 *   <li>Crédito personal: solo para clientes personales y máximo uno por persona.</li>
 *   <li>Crédito empresarial: solo para clientes empresariales, sin límite por empresa.</li>
 *   <li>No se exige que el cliente tenga una cuenta bancaria en la institución.</li>
 * </ul>
 */
public interface CreateCreditUseCase {

    /**
     * Crea y guarda un crédito para el cliente indicado.
     *
     * @param customerId id del cliente en customer-service
     * @param type       tipo de crédito a otorgar
     * @return el crédito guardado
     * @throws BusinessRuleException si el cliente no existe, si el tipo de crédito no
     *                               corresponde al tipo de cliente o si el cliente
     *                               personal ya tiene un crédito personal
     */
    Credit execute(Long customerId, CreditType type);
}
