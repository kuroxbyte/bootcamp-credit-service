package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

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
     * Fecha en que se otorgó el crédito.
     */
    @Setter private LocalDate grantedAt;

    /**
     * Crea el crédito validando que el tipo de crédito corresponda al tipo de cliente.
     *
     * @throws BusinessRuleException si el tipo de cliente no puede recibir este crédito
     */
    public Credit(Customer customer, CreditType type) {
        if (!type.allows(customer.type())) {
            throw new BusinessRuleException(
                    "El crédito " + type + " no está permitido para clientes " + customer.type());
        }
        this.customer = customer;
        this.type = type;
    }
}
