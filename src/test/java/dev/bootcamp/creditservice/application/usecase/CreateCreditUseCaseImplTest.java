package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditUseCaseImplTest {

    private static final Customer PERSONAL = new Customer(
            "1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);
    private static final Customer BUSINESS = new Customer(
            "2",
            CustomerType.BUSINESS,
            CustomerProfile.STANDARD);

    @Mock private CreditRepository creditRepository;

    @Mock private CustomerClient customerClient;

    @InjectMocks private CreateCreditUseCaseImpl useCase;

    private void givenSaveReturnsCredit() {
        when(creditRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById("99")).thenReturn(Maybe.empty());

        useCase.execute("99", CreditType.BUSINESS).test().assertError(NotFoundException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void createsFirstPersonalCredit() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(creditRepository.existsByCustomerIdAndType("1", CreditType.PERSONAL))
                .thenReturn(Single.just(false));
        givenSaveReturnsCredit();

        useCase.execute("1", CreditType.PERSONAL).test().assertComplete();

        verify(creditRepository).save(any(Credit.class));
    }

    @Test
    void rejectsSecondPersonalCredit() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(creditRepository.existsByCustomerIdAndType("1", CreditType.PERSONAL))
                .thenReturn(Single.just(true));

        useCase.execute("1", CreditType.PERSONAL).test().assertError(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void rejectsCreditTypeNotAllowedForCustomerType() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.execute("2", CreditType.PERSONAL).test().assertError(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void allowsMultipleBusinessCredits() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        givenSaveReturnsCredit();

        useCase.execute("2", CreditType.BUSINESS).test().assertComplete();
        useCase.execute("2", CreditType.BUSINESS).test().assertComplete();

        verify(creditRepository, never()).existsByCustomerIdAndType(any(), any());
        verify(creditRepository, times(2)).save(any(Credit.class));
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        givenSaveReturnsCredit();

        useCase.execute("2", CreditType.BUSINESS).test().assertComplete();

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById("2");
        verify(creditRepository).save(any(Credit.class));
        verifyNoMoreInteractions(customerClient, creditRepository);
    }
}
