package dev.bootcamp.creditservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCreditCardUseCaseImplTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private CreateCreditCardUseCaseImpl useCase;

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, LIMIT))
                .isInstanceOf(BusinessRuleException.class);
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    void createsCardForPersonalCustomer() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));
        when(creditCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditCard card = useCase.execute(1L, LIMIT);

        assertThat(card.getCreditLimit()).isEqualByComparingTo(LIMIT);
    }

    @Test
    void createsCardForBusinessCustomer() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(new Customer(2L, CustomerType.BUSINESS)));

        useCase.execute(2L, LIMIT);

        verify(creditCardRepository).save(any(CreditCard.class));
    }

    @Test
    void doesNotSaveCardWithoutPositiveCreditLimit() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));

        assertThatThrownBy(() -> useCase.execute(1L, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));

        useCase.execute(1L, LIMIT);

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById(1L);
        verify(creditCardRepository).save(any(CreditCard.class));
        verifyNoMoreInteractions(customerClient, creditCardRepository);
    }
}
