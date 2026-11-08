package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.util.EnumSet;
import java.util.Set;

/** Tipos de crédito y los tipos de cliente a los que se pueden otorgar. */
public enum CreditType {
    /** Solo para clientes personales (máximo uno por persona, validado en el caso de uso). */
    PERSONAL(EnumSet.of(CustomerType.PERSONAL)),
    /** Solo para clientes empresariales, más de uno por empresa. */
    BUSINESS(EnumSet.of(CustomerType.BUSINESS));

    private final Set<CustomerType> allowedCustomerTypes;

    CreditType(Set<CustomerType> allowedCustomerTypes) {
        this.allowedCustomerTypes = allowedCustomerTypes;
    }

    /** Indica si este tipo de crédito se puede otorgar al tipo de cliente indicado. */
    public boolean allows(CustomerType customerType) {
        return allowedCustomerTypes.contains(customerType);
    }
}
