package dev.bootcamp.creditservice.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
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

    private void givenSaveReturnsCard() {
        when(creditCardRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById("99")).thenReturn(Maybe.empty());

        useCase.execute("99", LIMIT).test().assertError(NotFoundException.class);
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    void createsCardForPersonalCustomer() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(new Customer("1", CustomerType.PERSONAL)));
        givenSaveReturnsCard();

        useCase.execute("1", LIMIT).test()
                .assertValue(card -> card.getCreditLimit().compareTo(LIMIT) == 0);
    }

    @Test
    void createsCardForBusinessCustomer() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(new Customer("2", CustomerType.BUSINESS)));
        givenSaveReturnsCard();

        useCase.execute("2", LIMIT).test().assertComplete();

        verify(creditCardRepository).save(any(CreditCard.class));
    }

    @Test
    void doesNotSaveCardWithoutPositiveCreditLimit() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(new Customer("1", CustomerType.PERSONAL)));

        useCase.execute("1", BigDecimal.ZERO).test().assertError(BusinessRuleException.class);
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(new Customer("1", CustomerType.PERSONAL)));
        givenSaveReturnsCard();

        useCase.execute("1", LIMIT).test().assertComplete();

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById("1");
        verify(creditCardRepository).save(any(CreditCard.class));
        verifyNoMoreInteractions(customerClient, creditCardRepository);
    }
}
