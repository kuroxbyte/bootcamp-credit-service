package dev.bootcamp.creditservice.domain.model.creditcard;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreditCardTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");

    @Test
    void allowedForPersonalOrBusinessCustomers() {
        assertThatCode(() -> new CreditCard(new Customer(1L, CustomerType.PERSONAL), LIMIT))
                .doesNotThrowAnyException();
        assertThatCode(() -> new CreditCard(new Customer(2L, CustomerType.BUSINESS), LIMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void requiresPositiveCreditLimit() {
        Customer customer = new Customer(1L, CustomerType.PERSONAL);

        assertThatThrownBy(() -> new CreditCard(customer, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new CreditCard(customer, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
    }
}
