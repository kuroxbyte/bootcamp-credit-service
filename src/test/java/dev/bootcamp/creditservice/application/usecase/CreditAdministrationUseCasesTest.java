package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CreditDebtClient;
import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditAdministrationUseCasesTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2027-01-10T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2027, 1, 10);
    private static final Customer CUSTOMER =
            new Customer("c1", CustomerType.PERSONAL, CustomerProfile.STANDARD);

    @Mock private CreditRepository creditRepository;

    @Mock private CreditCardRepository creditCardRepository;

    @Mock private CreditDebtClient creditDebtClient;

    @Mock private CreditEventPublisher creditEventPublisher;

    private static Credit credit() {
        Credit credit = new Credit(
                CUSTOMER, CreditType.PERSONAL, new BigDecimal("1000"), LocalDate.of(2027, 6, 1));
        credit.setId("k1");
        credit.setGrantedAt(LocalDate.of(2026, 11, 15));
        return credit;
    }

    private static CreditCard card() {
        CreditCard card = new CreditCard(CUSTOMER, new BigDecimal("5000"), 15);
        card.setId("t1");
        card.setGrantedAt(LocalDate.of(2026, 11, 15));
        return card;
    }

    private UpdateCreditUseCaseImpl credits() {
        return new UpdateCreditUseCaseImpl(
                creditRepository, creditDebtClient, creditEventPublisher, CLOCK);
    }

    private UpdateCreditCardUseCaseImpl cards() {
        return new UpdateCreditCardUseCaseImpl(
                creditCardRepository, creditDebtClient, creditEventPublisher, CLOCK);
    }

    @Test
    void refinancesCreditAndPublishesIt() {
        when(creditRepository.findById("k1")).thenReturn(Maybe.just(credit()));
        when(creditRepository.save(any()))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(creditEventPublisher.publishCreditUpdated(any())).thenReturn(Completable.complete());

        credits().update("k1", LocalDate.of(2028, 1, 1)).test()
                .assertValue(updated -> updated.getDueDate().equals(LocalDate.of(2028, 1, 1))
                        && updated.getId().equals("k1"));
    }

    @Test
    void newDueDateMustBeAfterTheGrantDate() {
        assertThatThrownBy(() -> credit().withDueDate(LocalDate.of(2026, 11, 15)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> credit().withDueDate(null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void closesCreditOnlyWhenFullyPaid() {
        Credit credit = credit();
        when(creditRepository.findById("k1")).thenReturn(Maybe.just(credit));
        when(creditDebtClient.paidAmount("k1", "c1"))
                .thenReturn(
                        Single.just(new BigDecimal("400")),
                        Single.just(new BigDecimal("1000")));
        when(creditRepository.save(credit)).thenReturn(Single.just(credit));
        when(creditEventPublisher.publishCreditClosed(credit)).thenReturn(Completable.complete());

        credits().close("k1").test().assertError(BusinessRuleException.class);
        credits().close("k1").test().assertComplete();

        assertThat(credit.getClosedAt()).isEqualTo(TODAY);
    }

    @Test
    void closedCreditIsNotFound() {
        Credit closed = credit();
        closed.close(TODAY);
        when(creditRepository.findById("k1")).thenReturn(Maybe.just(closed));

        credits().close("k1").test().assertError(NotFoundException.class);
        new GetCreditsUseCaseImpl(creditRepository).findById("k1").test()
                .assertError(NotFoundException.class);
    }

    @Test
    void changesCardTermsUnlessTheLimitIsBelowTheDebt() {
        when(creditCardRepository.findById("t1")).thenReturn(Maybe.just(card()));
        // Límite 5000 y disponible 3000: debe 2000.
        when(creditDebtClient.availableCredit("t1", "c1")).thenReturn(Single.just(
                new BigDecimal("3000")));
        when(creditCardRepository.save(any()))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(creditEventPublisher.publishCreditCardUpdated(any()))
                .thenReturn(Completable.complete());

        cards().update("t1", new BigDecimal("1500"), 20).test()
                .assertError(BusinessRuleException.class);
        cards().update("t1", new BigDecimal("2500"), 20).test()
                .assertValue(updated -> updated.getPaymentDueDay() == 20
                        && updated.getCreditLimit().compareTo(new BigDecimal("2500")) == 0);
    }

    @Test
    void closesCardOnlyWithoutDebt() {
        CreditCard card = card();
        when(creditCardRepository.findById("t1")).thenReturn(Maybe.just(card));
        when(creditDebtClient.availableCredit("t1", "c1"))
                .thenReturn(
                        Single.just(new BigDecimal("4999")),
                        Single.just(new BigDecimal("5000")));
        when(creditCardRepository.save(card)).thenReturn(Single.just(card));
        when(creditEventPublisher.publishCreditCardClosed(card))
                .thenReturn(Completable.complete());

        cards().close("t1").test().assertError(BusinessRuleException.class);
        cards().close("t1").test().assertComplete();

        assertThat(card.getClosedAt()).isEqualTo(TODAY);
    }

    @Test
    void closedCardIsNotFound() {
        CreditCard closed = card();
        closed.close(TODAY);
        when(creditCardRepository.findById("t1")).thenReturn(Maybe.just(closed));

        cards().update("t1", BigDecimal.TEN, 10).test().assertError(NotFoundException.class);
        new GetCreditCardsUseCaseImpl(creditCardRepository).findById("t1").test()
                .assertError(NotFoundException.class);
        verify(creditDebtClient, never()).availableCredit(any(), any());
    }
}
