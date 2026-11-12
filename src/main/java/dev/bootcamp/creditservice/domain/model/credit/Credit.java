package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Crédito personal o empresarial.
 */
@Getter
public class Credit {
    @Setter private String id;
    private final Customer customer;
    private final CreditType type;

    /**
     * Monto otorgado; la deuda es este monto menos los pagos.
     */
    private final BigDecimal amount;

    /**
     * Fecha de vencimiento: desde el día siguiente, el saldo que quede es deuda vencida.
     */
    private final LocalDate dueDate;

    /**
     * Fecha en que se otorgó el crédito.
     */
    @Setter private LocalDate grantedAt;

    /**
     * Crea el crédito validando sus reglas.
     *
     * @throws BusinessRuleException si el tipo de cliente no puede recibir este crédito, el monto
     *     no es mayor a 0 o falta la fecha de vencimiento
     */
    public Credit(
            Customer customer,
            CreditType type,
            BigDecimal amount,
            LocalDate dueDate) {
        if (!type.allows(customer.type())) {
            throw new BusinessRuleException(
                    "El crédito " + type + " no está permitido para clientes " + customer.type());
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("El monto del crédito debe ser mayor a 0");
        }
        if (dueDate == null) {
            throw new BusinessRuleException("El crédito debe tener fecha de vencimiento");
        }
        this.customer = customer;
        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    /**
     * Fija la fecha de otorgamiento validando que el crédito venza después.
     *
     * @throws BusinessRuleException si la fecha de vencimiento no es posterior a la de otorgamiento
     */
    public void grant(LocalDate today) {
        if (!dueDate.isAfter(today)) {
            throw new BusinessRuleException(
                    "La fecha de vencimiento debe ser posterior a la fecha de otorgamiento");
        }
        this.grantedAt = today;
    }
}
