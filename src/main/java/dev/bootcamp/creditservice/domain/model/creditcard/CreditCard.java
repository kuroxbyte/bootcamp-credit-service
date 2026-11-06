package dev.bootcamp.creditservice.domain.model.creditcard;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Tarjeta de crédito, para clientes personales o empresariales.
 * El límite de crédito define cuánto puede consumir el cliente.
 */
@Getter
public class CreditCard {
    @Setter
    private String id;
    private final Customer customer;
    private final BigDecimal creditLimit;

    public CreditCard(Customer customer, BigDecimal creditLimit) {
        if (creditLimit == null || creditLimit.signum() <= 0) {
            throw new BusinessRuleException("La tarjeta de crédito debe tener un límite de crédito mayor a 0");
        }
        this.customer = customer;
        this.creditLimit = creditLimit;
    }
}
