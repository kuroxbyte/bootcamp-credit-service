package dev.bootcamp.creditservice.domain.model.creditcard;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditCardTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");

    @Test
    void allowedForPersonalOrBusinessCustomers() {
        assertThatCode(
                () -> new CreditCard(new Customer(
                        "1",
                        CustomerType.PERSONAL,
                        CustomerProfile.STANDARD),
                LIMIT))
                .doesNotThrowAnyException();
        assertThatCode(
                () -> new CreditCard(new Customer(
                        "2",
                        CustomerType.BUSINESS,
                        CustomerProfile.STANDARD),
                LIMIT))
                .doesNotThrowAnyException();
    }

    @Test
    void requiresPositiveCreditLimit() {
        Customer customer = new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD);

        assertThatThrownBy(() -> new CreditCard(customer, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new CreditCard(customer, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
    }
}
