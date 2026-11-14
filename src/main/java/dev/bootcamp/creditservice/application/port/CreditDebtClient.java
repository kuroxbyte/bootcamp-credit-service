package dev.bootcamp.creditservice.application.port;

import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Consulta en transaction-service, que guarda los pagos y consumos, de lo que se debe de un
 * producto de crédito.
 */
public interface CreditDebtClient {

    /**
     * Suma de los pagos hechos al crédito.
     *
     * @return el total pagado; error si transaction-service no responde
     */
    Single<BigDecimal> paidAmount(String creditId, String customerId);

    /**
     * Crédito disponible de la tarjeta (límite menos consumos más pagos).
     *
     * @return el disponible; error si transaction-service no responde
     */
    Single<BigDecimal> availableCredit(String creditCardId, String customerId);
}
