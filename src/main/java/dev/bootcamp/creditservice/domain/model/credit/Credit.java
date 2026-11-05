package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Credit {
    @Setter
    private Long id;
    private final Customer customer;
    private final CreditType type;

    public Credit(Customer customer, CreditType type) {
        if (type == CreditType.PERSONAL && customer.type() != CustomerType.PERSONAL) {
            throw new BusinessRuleException("El crédito personal solo es para clientes personales");
        }
        if (type == CreditType.BUSINESS && customer.type() != CustomerType.BUSINESS) {
            throw new BusinessRuleException("El crédito empresarial solo es para clientes empresariales");
        }
        // CREDIT_CARD: permitido para clientes personales y empresariales.
        this.customer = customer;
        this.type = type;
    }
}
