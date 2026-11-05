package dev.bootcamp.creditservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCreditUseCaseImplTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private CreateCreditUseCaseImpl useCase;

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, CreditType.CREDIT_CARD))
                .isInstanceOf(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void createsFirstPersonalCredit() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));
        when(creditRepository.existsByCustomerIdAndType(1L, CreditType.PERSONAL)).thenReturn(false);

        useCase.execute(1L, CreditType.PERSONAL);

        verify(creditRepository).save(any(Credit.class));
    }

    @Test
    void rejectsSecondPersonalCredit() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));
        when(creditRepository.existsByCustomerIdAndType(1L, CreditType.PERSONAL)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, CreditType.PERSONAL))
                .isInstanceOf(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void allowsMultipleBusinessCredits() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(new Customer(2L, CustomerType.BUSINESS)));

        useCase.execute(2L, CreditType.BUSINESS);
        useCase.execute(2L, CreditType.BUSINESS);

        verify(creditRepository, never()).existsByCustomerIdAndType(any(), any());
        verify(creditRepository, times(2)).save(any(Credit.class));
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(new Customer(1L, CustomerType.PERSONAL)));

        useCase.execute(1L, CreditType.CREDIT_CARD);

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById(1L);
        verify(creditRepository).save(any(Credit.class));
        verifyNoMoreInteractions(customerClient, creditRepository);
    }
}
