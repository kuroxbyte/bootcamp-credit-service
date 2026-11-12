package dev.bootcamp.creditservice.domain.model.debt;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Estado de deuda de un cliente, copiado del evento {@code CustomerDebtStatusChanged} de
 * debt-service.
 *
 * @param customerId     id del cliente
 * @param hasOverdueDebt true si tiene deuda vencida en algún producto de crédito
 * @param overdueAmount  monto vencido (0 si no tiene)
 * @param occurredAt     momento en que debt-service calculó el estado
 */
public record CustomerDebtStatus(
        String customerId,
        boolean hasOverdueDebt,
        BigDecimal overdueAmount,
        Instant occurredAt) {}
