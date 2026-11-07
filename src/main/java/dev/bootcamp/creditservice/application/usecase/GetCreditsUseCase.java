package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para consultar créditos personales y empresariales.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe.
 */
public interface GetCreditsUseCase {

    /**
     * Lista los productos de este tipo que tiene un cliente.
     *
     * @param customerId id del cliente en customer-service
     * @return los productos del cliente; ninguno si no tiene
     */
    Flowable<Credit> findByCustomerId(String customerId);

    /**
     * Consulta un producto por su id.
     *
     * @param id id del producto
     * @return el crédito; error {@link NotFoundException} si no existe
     */
    Single<Credit> findById(String id);
}
