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
     * Fecha en que se otorgó la tarjeta.
     */
    @Setter private LocalDate grantedAt;

    /**
     * Crea la tarjeta validando su límite de crédito.
     *
     * @throws BusinessRuleException si el límite de crédito no es mayor a 0
     */
    public CreditCard(Customer customer, BigDecimal creditLimit) {
        if (creditLimit == null || creditLimit.signum() <= 0) {
            throw new BusinessRuleException(
                    "La tarjeta de crédito debe tener un límite de crédito mayor a 0");
        }
        this.customer = customer;
        this.creditLimit = creditLimit;
    }
}
