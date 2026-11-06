package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Credit {
    @Setter
    private Long id;
    private final Customer customer;
    private final CreditType type;
    /** Límite de crédito; solo para tipos que lo requieren (tarjeta de crédito), si no es null. */
    private final BigDecimal creditLimit;

    public Credit(Customer customer, CreditType type, BigDecimal creditLimit) {
        if (!type.allows(customer.type())) {
            throw new BusinessRuleException(
                    "El crédito " + type + " no está permitido para clientes " + customer.type());
        }
        if (type.requiresCreditLimit() && (creditLimit == null || creditLimit.signum() <= 0)) {
            throw new BusinessRuleException("La tarjeta de crédito debe tener un límite de crédito mayor a 0");
        }
        if (!type.requiresCreditLimit() && creditLimit != null) {
            throw new BusinessRuleException("El crédito " + type + " no tiene límite de crédito");
        }
        this.customer = customer;
        this.type = type;
        this.creditLimit = creditLimit;
    }
}
