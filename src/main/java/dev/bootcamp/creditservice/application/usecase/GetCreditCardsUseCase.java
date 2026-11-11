package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para consultar tarjetas de crédito.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe.
 */
public interface GetCreditCardsUseCase {

    /**
     * Lista los productos de este tipo que tiene un cliente.
     *
     * @param customerId id del cliente en customer-service
     * @return los productos del cliente; ninguno si no tiene
     */
    Flowable<CreditCard> findByCustomerId(String customerId);

    /**
     * Consulta un producto por su id.
     *
     * @param id id del producto
     * @return la tarjeta; error {@link NotFoundException} si no existe
     */
    Single<CreditCard> findById(String id);

    /**
     * Lista todas las tarjetas de crédito del banco; lo usa transaction-service para los reportes.
     *
     * @return todos los productos de este tipo; ninguno si no hay
     */
    Flowable<CreditCard> findAll();
}
