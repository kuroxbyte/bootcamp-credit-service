package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditTest {

    private static final Customer PERSONAL = new Customer("1", CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer("2", CustomerType.BUSINESS);

    @Test
    void personalCreditOnlyForPersonalCustomers() {
        assertThatCode(() -> new Credit(PERSONAL, CreditType.PERSONAL)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new Credit(BUSINESS, CreditType.PERSONAL))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void businessCreditOnlyForBusinessCustomers() {
        assertThatCode(() -> new Credit(BUSINESS, CreditType.BUSINESS)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new Credit(PERSONAL, CreditType.BUSINESS))
                .isInstanceOf(BusinessRuleException.class);
    }
}
