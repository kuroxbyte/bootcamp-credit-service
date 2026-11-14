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
     * Indica si el cliente ya tiene un crédito activo del tipo indicado.
     */
    Single<Boolean> existsByCustomerIdAndType(String customerId, CreditType type);

    /**
     * Busca el crédito por su id, aunque esté dado de baja.
     *
     * @return el crédito, o vacío si no existe
     */
    Maybe<Credit> findById(String creditId);

    /**
     * Busca los productos del cliente.
     *
     * @return los créditos activos del cliente (ninguno si no tiene)
     */
    Flowable<Credit> findByCustomerId(String customerId);

    /**
     * Guarda el crédito y lo emite con su id asignado.
     */
    Single<Credit> save(Credit credit);

    /**
     * Todos los créditos del banco, también los dados de baja.
     */
    Flowable<Credit> findAll();
}
