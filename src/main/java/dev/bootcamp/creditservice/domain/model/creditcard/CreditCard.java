package dev.bootcamp.creditservice.domain.model.creditcard;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tarjeta de crédito, para clientes personales o empresariales. El límite de crédito define cuánto
 * puede consumir el cliente.
 */
@Getter
public class CreditCard {
    @Setter private String id;
    private final Customer customer;
    private final BigDecimal creditLimit;

    /**
     * Día del mes (1 a 28) hasta el que se pagan los consumos del mes anterior.
     */
    private final int paymentDueDay;

    /**
     * Fecha en que se otorgó la tarjeta.
     */
    @Setter private LocalDate grantedAt;

    /**
     * Crea la tarjeta validando su límite de crédito y su día de pago.
     *
     * @throws BusinessRuleException si el límite de crédito no es mayor a 0 o el día de pago no
     *     está entre 1 y 28 (para que exista en todos los meses)
     */
    public CreditCard(
            Customer customer,
            BigDecimal creditLimit,
            int paymentDueDay) {
        if (creditLimit == null || creditLimit.signum() <= 0) {
            throw new BusinessRuleException(
                    "La tarjeta de crédito debe tener un límite de crédito mayor a 0");
        }
        if (paymentDueDay < 1 || paymentDueDay > 28) {
            throw new BusinessRuleException("El día de pago debe estar entre 1 y 28");
        }
        this.customer = customer;
        this.creditLimit = creditLimit;
        this.paymentDueDay = paymentDueDay;
    }
}
