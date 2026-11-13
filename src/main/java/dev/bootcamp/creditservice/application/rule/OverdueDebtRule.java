package dev.bootcamp.creditservice.application.rule;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Regla común a todos los productos: un cliente con deuda vencida en algún producto de crédito no
 * puede adquirir otro producto.
 *
 * <p>Usa la copia local del estado de deuda que publica debt-service; si no hay estado para el
 * cliente, se considera sin deuda vencida.
 */
@RequiredArgsConstructor
public class OverdueDebtRule {

    static final String MESSAGE = "El cliente tiene una deuda vencida en un producto de crédito";

    private final CustomerDebtStatusRepository customerDebtStatusRepository;

    /**
     * Verifica que el cliente no tenga deuda vencida.
     *
     * @return termina sin error si no tiene; error {@link BusinessRuleException} si tiene
     */
    public Completable verify(String customerId) {
        return customerDebtStatusRepository
                .hasOverdueDebt(customerId)
                .flatMapCompletable(overdue -> overdue
                        ? Completable.error(new BusinessRuleException(MESSAGE))
                        : Completable.complete());
    }
}
