package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de los créditos personales y empresariales.
 */
public interface CreditRepository {
    /**
     * Indica si el cliente ya tiene un crédito del tipo indicado.
     */
    Single<Boolean> existsByCustomerIdAndType(String customerId, CreditType type);

    /**
     * Busca el recurso por su id.
     *
     * @return el crédito, o vacío si no existe
     */
    Maybe<Credit> findById(String creditId);

    /**
     * Busca los productos del cliente.
     *
     * @return los créditos del cliente (ninguno si no tiene)
     */
    Flowable<Credit> findByCustomerId(String customerId);

    /**
     * Guarda el crédito y lo emite con su id asignado.
     */
    Single<Credit> save(Credit credit);
}
