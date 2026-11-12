package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.application.rule.OverdueDebtRule;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Mock private CustomerDebtStatusRepository customerDebtStatusRepository;

    @Mock private CreditEventPublisher creditEventPublisher;

    private CreateCreditUseCaseImpl useCase;

    /**
     * Reloj fijo: la fecha de otorgamiento es 2026-11-15.
     */
    @BeforeEach
    void createUseCase() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        useCase = new CreateCreditUseCaseImpl(
                creditRepository,
                customerClient,
                new OverdueDebtRule(customerDebtStatusRepository),
                creditEventPublisher,
                clock);
        when(customerDebtStatusRepository.hasOverdueDebt(any())).thenReturn(Single.just(false));
        when(creditEventPublisher.publishCreditGranted(any()))
                .thenReturn(Completable.complete());
    }

    private void givenSaveReturnsCredit() {
        when(creditRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById("99")).thenReturn(Maybe.empty());

        useCase.execute(
                "99",
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertError(NotFoundException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void createsFirstPersonalCredit() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(creditRepository.existsByCustomerIdAndType("1", CreditType.PERSONAL))
                .thenReturn(Single.just(false));
        givenSaveReturnsCredit();

        useCase.execute(
                "1",
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertComplete();

        verify(creditRepository).save(any(Credit.class));
    }

    @Test
    void rejectsSecondPersonalCredit() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(creditRepository.existsByCustomerIdAndType("1", CreditType.PERSONAL))
                .thenReturn(Single.just(true));

        useCase.execute(
                "1",
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertError(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void rejectsCreditTypeNotAllowedForCustomerType() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.execute(
                "2",
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertError(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }

    @Test
    void allowsMultipleBusinessCredits() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        givenSaveReturnsCredit();

        useCase.execute(
                "2",
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertComplete();
        useCase.execute(
                "2",
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertComplete();

        verify(creditRepository, never()).existsByCustomerIdAndType(any(), any());
        verify(creditRepository, times(2)).save(any(Credit.class));
    }

    @Test
    void doesNotRequireBankAccount() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        givenSaveReturnsCredit();

        useCase.execute(
                "2",
                CreditType.BUSINESS,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)).test().assertComplete();

        // Solo se consulta al cliente; no hay consulta a cuentas bancarias.
        verify(customerClient).findById("2");
        verify(creditRepository).save(any(Credit.class));
        verifyNoMoreInteractions(customerClient, creditRepository);
    }

    @Test
    void setsGrantedDateToToday() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        when(creditRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("2", CreditType.BUSINESS, BigDecimal.TEN, LocalDate.of(2027, 1, 15)).test()
                .assertValue(credit -> credit.getGrantedAt().equals(LocalDate.of(2026, 11, 15)));
    }

    @Test
    void rejectsCustomerWithOverdueDebtWithoutSavingOrPublishing() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        when(customerDebtStatusRepository.hasOverdueDebt("2")).thenReturn(Single.just(true));

        useCase.execute("2", CreditType.BUSINESS, BigDecimal.TEN, LocalDate.of(2027, 1, 15))
                .test()
                .assertError(error -> error instanceof BusinessRuleException
                        && error.getMessage().equals(
                                "El cliente tiene una deuda vencida en un producto de crédito"));
        verify(creditRepository, never()).save(any());
        verify(creditEventPublisher, never()).publishCreditGranted(any());
    }

    @Test
    void publishesCreditGrantedAfterSaving() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        givenSaveReturnsCredit();

        Credit credit = useCase
                .execute("2", CreditType.BUSINESS, BigDecimal.TEN, LocalDate.of(2027, 1, 15))
                .blockingGet();

        verify(creditEventPublisher).publishCreditGranted(credit);
    }

    @Test
    void rejectsDueDateThatIsNotAfterToday() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.execute("2", CreditType.BUSINESS, BigDecimal.TEN, LocalDate.of(2026, 11, 15))
                .test()
                .assertError(BusinessRuleException.class);
        verify(creditRepository, never()).save(any());
    }
}
