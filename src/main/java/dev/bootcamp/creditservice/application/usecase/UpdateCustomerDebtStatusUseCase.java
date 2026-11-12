package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para actualizar la copia local del estado de deuda de un cliente (evento
 * {@code CustomerDebtStatusChanged} de debt-service).
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Completable}.
 */
public interface UpdateCustomerDebtStatusUseCase {

    /**
     * Guarda o reemplaza el estado de deuda del cliente.
     */
    Completable execute(CustomerDebtStatus status);
}
