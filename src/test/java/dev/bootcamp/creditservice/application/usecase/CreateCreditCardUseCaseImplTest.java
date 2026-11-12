package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.application.rule.OverdueDebtRule;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateCreditCardUseCaseImplTest {

    private static final BigDecimal LIMIT = new BigDecimal("1000");

    @Mock private CreditCardRepository creditCardRepository;

    @Mock private CustomerClient customerClient;

    @Mock private CustomerDebtStatusRepository customerDebtStatusRepository;

    @Mock private CreditEventPublisher creditEventPublisher;

    private CreateCreditCardUseCaseImpl useCase;

    /**
     * Reloj fijo: la fecha de otorgamiento es 2026-11-15.
     */
    @BeforeEach
    void createUseCase() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        useCase = new CreateCreditCardUseCaseImpl(
                creditCardRepository,
                customerClient,
                new OverdueDebtRule(customerDebtStatusRepository),
                creditEventPublisher,
                clock);
        when(customerDebtStatusRepository.hasOverdueDebt(any())).thenReturn(Single.just(false));
        when(creditEventPublisher.publishCreditCardGranted(any()))
                .thenReturn(Completable.complete());
    }

    private void givenSaveReturnsCard() {
        when(creditCardRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById("99")).thenReturn(Maybe.empty());

        useCase.execute("99", LIMIT, 15).test().assertError(NotFoundException.class);
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

        useCase.execute("1", LIMIT, 15)
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

        useCase.execute("2", LIMIT, 15).test().assertComplete();

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

        useCase.execute("1", BigDecimal.ZERO, 15).test().assertError(BusinessRuleException.class);
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

        useCase.execute("1", LIMIT, 15).test().assertComplete();

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

        useCase.execute("2", LIMIT, 15).test()
                .assertValue(card -> card.getGrantedAt().equals(LocalDate.of(2026, 11, 15)));
    }

    @Test
    void rejectsCustomerWithOverdueDebtWithoutSavingOrPublishing() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(
                new Customer("2", CustomerType.BUSINESS, CustomerProfile.STANDARD)));
        when(customerDebtStatusRepository.hasOverdueDebt("2")).thenReturn(Single.just(true));

        useCase.execute("2", LIMIT, 15).test().assertError(BusinessRuleException.class);
        verify(creditCardRepository, never()).save(any());
        verify(creditEventPublisher, never()).publishCreditCardGranted(any());
    }

    @Test
    void publishesCreditCardGrantedAfterSaving() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(
                new Customer("2", CustomerType.BUSINESS, CustomerProfile.STANDARD)));
        when(creditCardRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        CreditCard card = useCase.execute("2", LIMIT, 15).blockingGet();

        verify(creditEventPublisher).publishCreditCardGranted(card);
    }
}
