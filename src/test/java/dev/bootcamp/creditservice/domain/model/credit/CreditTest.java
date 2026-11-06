package dev.bootcamp.creditservice.domain.model.credit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreditTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");
    private static final Customer PERSONAL = new Customer(1L, CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer(2L, CustomerType.BUSINESS);

    @Test
    void personalCreditOnlyForPersonalCustomers() {
        assertThatCode(() -> new Credit(PERSONAL, CreditType.PERSONAL, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new Credit(BUSINESS, CreditType.PERSONAL, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void businessCreditOnlyForBusinessCustomers() {
        assertThatCode(() -> new Credit(BUSINESS, CreditType.BUSINESS, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new Credit(PERSONAL, CreditType.BUSINESS, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void creditCardForPersonalOrBusinessCustomers() {
        assertThatCode(() -> new Credit(PERSONAL, CreditType.CREDIT_CARD, LIMIT)).doesNotThrowAnyException();
        assertThatCode(() -> new Credit(BUSINESS, CreditType.CREDIT_CARD, LIMIT)).doesNotThrowAnyException();
    }

    @Test
    void creditCardRequiresPositiveCreditLimit() {
        assertThatThrownBy(() -> new Credit(PERSONAL, CreditType.CREDIT_CARD, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Credit(PERSONAL, CreditType.CREDIT_CARD, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void otherCreditsDoNotHaveCreditLimit() {
        assertThatThrownBy(() -> new Credit(PERSONAL, CreditType.PERSONAL, LIMIT))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Credit(BUSINESS, CreditType.BUSINESS, LIMIT))
                .isInstanceOf(BusinessRuleException.class);
    }
}
