package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditCardUseCaseImplTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");

    @Mock private CreditCardRepository creditCardRepository;

    @Mock private CustomerClient customerClient;

    private CreateCreditCardUseCaseImpl useCase;

    /**
     * Reloj fijo: la fecha de otorgamiento es 2026-11-15.
     */
    @BeforeEach
    void createUseCase() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        useCase = new CreateCreditCardUseCaseImpl(creditCardRepository, customerClient, clock);
    }

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
        when(customerClient.findById("1"))
                .thenReturn(
                        Maybe.just(
                                new Customer(
                                        "1",
                                        CustomerType.PERSONAL,
                                        CustomerProfile.STANDARD)));
        givenSaveReturnsCard();

        useCase.execute("1", LIMIT)
                .test()
                .assertValue(card -> card.getCreditLimit().compareTo(LIMIT) == 0);
    }

    @Test
    void createsCardForBusinessCustomer() {
        when(customerClient.findById("2"))
                .thenReturn(
                        Maybe.just(
                                new Customer(
                                        "2",
                                        CustomerType.BUSINESS,
                                        CustomerProfile.STANDARD)));
        givenSaveReturnsCard();

        useCase.execute("2", LIMIT).test().assertComplete();

        verify(creditCardRepository).save(any(CreditCard.class));
    }

    @Test
    void doesNotSaveCardWithoutPositiveCreditLimit() {
        when(customerClient.findById("1"))
                .thenReturn(
                        Maybe.just(
                                new Customer(
                                        "1",
                                        CustomerType.PERSONAL,
                                        CustomerProfile.STANDARD)));

        useCase.execute("1", BigDecimal.ZERO).test().assertError(BusinessRuleException.class);
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById("1"))
                .thenReturn(
                        Maybe.just(
                                new Customer(
                                        "1",
                                        CustomerType.PERSONAL,
                                        CustomerProfile.STANDARD)));
        givenSaveReturnsCard();

        useCase.execute("1", LIMIT).test().assertComplete();

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById("1");
        verify(creditCardRepository).save(any(CreditCard.class));
        verifyNoMoreInteractions(customerClient, creditCardRepository);
    }

    @Test
    void setsGrantedDateToToday() {
        Customer business = new Customer("2", CustomerType.BUSINESS, CustomerProfile.STANDARD);
        when(customerClient.findById("2")).thenReturn(Maybe.just(business));
        when(creditCardRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("2", LIMIT).test()
                .assertValue(card -> card.getGrantedAt().equals(LocalDate.of(2026, 11, 15)));
    }
}
