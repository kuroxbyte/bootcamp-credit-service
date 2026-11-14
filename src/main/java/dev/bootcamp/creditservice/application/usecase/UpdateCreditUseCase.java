package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.time.LocalDate;

/**
 * Casos de uso del empleado del banco sobre un crédito: refinanciar su vencimiento y darlo de
 * baja. Publican {@code CreditUpdated} y {@code CreditClosed}.
 *
 * <p>Son reactivos: no hacen nada hasta que se suscribe.
 */
public interface UpdateCreditUseCase {

    /**
     * Cambia la fecha de vencimiento; debt-service recalcula la deuda vencida con la nueva fecha.
     *
     * @return el crédito modificado; error {@link NotFoundException} si no existe o fue dado de
     *     baja, o {@link BusinessRuleException} si la fecha no es posterior al otorgamiento
     */
    Single<Credit> update(String creditId, LocalDate dueDate);

    /**
     * Da de baja el crédito (baja lógica). Solo si ya está pagado por completo.
     *
     * @return termina sin error si se dio de baja; error {@link NotFoundException} si no existe o
     *     ya fue dado de baja, o {@link BusinessRuleException} si todavía tiene deuda
     */
    Completable close(String creditId);
}
