package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Credit {
    @Setter
    private Long id;
    private final Customer customer;
    private final CreditType type;

    public Credit(Customer customer, CreditType type) {
        if (!type.allows(customer.type())) {
            throw new BusinessRuleException(
                    "El crédito " + type + " no está permitido para clientes " + customer.type());
        }
        this.customer = customer;
        this.type = type;
    }
}
