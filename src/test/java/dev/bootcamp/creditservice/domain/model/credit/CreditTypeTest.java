package dev.bootcamp.creditservice.domain.model.credit;

import static org.assertj.core.api.Assertions.assertThat;

import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

class CreditTypeTest {

    @Test
    void personalAllowsOnlyPersonalCustomers() {
        assertThat(CreditType.PERSONAL.allows(CustomerType.PERSONAL)).isTrue();
        assertThat(CreditType.PERSONAL.allows(CustomerType.BUSINESS)).isFalse();
    }

    @Test
    void businessAllowsOnlyBusinessCustomers() {
        assertThat(CreditType.BUSINESS.allows(CustomerType.BUSINESS)).isTrue();
        assertThat(CreditType.BUSINESS.allows(CustomerType.PERSONAL)).isFalse();
    }

    @Test
    void creditCardAllowsBothCustomerTypes() {
        assertThat(CreditType.CREDIT_CARD.allows(CustomerType.PERSONAL)).isTrue();
        assertThat(CreditType.CREDIT_CARD.allows(CustomerType.BUSINESS)).isTrue();
    }
}
