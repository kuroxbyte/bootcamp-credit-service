package dev.bootcamp.creditservice.domain.model.credit;

import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditTest {

    private static final Customer PERSONAL = new Customer(
            "1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);
    private static final Customer BUSINESS = new Customer(
            "2",
            CustomerType.BUSINESS,
            CustomerProfile.STANDARD);

    @Test
    void personalCreditOnlyForPersonalCustomers() {
        assertThatCode(
                () -> new Credit(PERSONAL,
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15))).doesNotThrowAnyException();
        assertThatThrownBy(
                () -> new Credit(BUSINESS,
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void businessCreditOnlyForBusinessCustomers() {
        assertThatCode(
                () -> new Credit(BUSINESS,
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15))).doesNotThrowAnyException();
        assertThatThrownBy(
                () -> new Credit(PERSONAL,
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void requiresPositiveAmountAndDueDate() {
        Customer customer = new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD);

        assertThatThrownBy(() -> new Credit(
                        customer,
                        CreditType.PERSONAL,
                        BigDecimal.ZERO,
                        LocalDate.of(2027, 1, 15)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El monto del crédito debe ser mayor a 0");
        assertThatThrownBy(() -> new Credit(customer, CreditType.PERSONAL, BigDecimal.TEN, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void grantsOnlyWhenDueDateIsAfterToday() {
        Customer customer = new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD);
        Credit credit = new Credit(
                customer,
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2026, 11, 16));

        assertThatThrownBy(() -> credit.grant(LocalDate.of(2026, 11, 16)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(
                        "La fecha de vencimiento debe ser posterior a la fecha de otorgamiento");
        credit.grant(LocalDate.of(2026, 11, 15));
        assertThat(credit.getGrantedAt()).isEqualTo(LocalDate.of(2026, 11, 15));
    }
}
