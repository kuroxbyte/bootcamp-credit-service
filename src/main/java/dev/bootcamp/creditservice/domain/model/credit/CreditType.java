package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.util.EnumSet;
import java.util.Set;

/**
 * Tipos de crédito, los tipos de cliente a los que se pueden otorgar
 * y si requieren límite de crédito.
 */
public enum CreditType {
    /** Solo para clientes personales (máximo uno por persona, validado en el caso de uso). */
    PERSONAL(EnumSet.of(CustomerType.PERSONAL), false),
    /** Solo para clientes empresariales, más de uno por empresa. */
    BUSINESS(EnumSet.of(CustomerType.BUSINESS), false),
    /** Para clientes personales o empresariales, con un límite de crédito para sus consumos. */
    CREDIT_CARD(EnumSet.of(CustomerType.PERSONAL, CustomerType.BUSINESS), true);

    private final Set<CustomerType> allowedCustomerTypes;
    private final boolean requiresCreditLimit;

    CreditType(Set<CustomerType> allowedCustomerTypes, boolean requiresCreditLimit) {
        this.allowedCustomerTypes = allowedCustomerTypes;
        this.requiresCreditLimit = requiresCreditLimit;
    }

    /**
     * Indica si este tipo de crédito se puede otorgar al tipo de cliente indicado.
     */
    public boolean allows(CustomerType customerType) {
        return allowedCustomerTypes.contains(customerType);
    }

    /**
     * Indica si este tipo de crédito debe tener un límite de crédito.
     */
    public boolean requiresCreditLimit() {
        return requiresCreditLimit;
    }
}
