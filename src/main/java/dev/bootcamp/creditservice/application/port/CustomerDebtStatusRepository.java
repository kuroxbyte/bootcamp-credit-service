package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Copia local del estado de deuda de cada cliente (datos maestros), con caché.
 */
public interface CustomerDebtStatusRepository {

    /**
     * Guarda o reemplaza el estado del cliente.
     */
    Completable save(CustomerDebtStatus status);

    /**
     * Indica si el cliente tiene deuda vencida; false si no se conoce ningún estado para él.
     */
    Single<Boolean> hasOverdueDebt(String customerId);
}
