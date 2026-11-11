package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCreditCardsUseCaseImplTest {

    private static final CreditCard CARD =
            new CreditCard(
                    new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD),
                    new BigDecimal("1000"));

    @Mock private CreditCardRepository creditCardRepository;

    @InjectMocks private GetCreditCardsUseCaseImpl useCase;

    @Test
    void listsCardsOfCustomer() {
        when(creditCardRepository.findByCustomerId("1")).thenReturn(Flowable.just(CARD));

        useCase.findByCustomerId("1").test().assertValues(CARD);
    }

    @Test
    void returnsExistingCard() {
        when(creditCardRepository.findById("7")).thenReturn(Maybe.just(CARD));

        useCase.findById("7").test().assertValue(CARD);
    }

    @Test
    void failsWhenCardDoesNotExist() {
        when(creditCardRepository.findById("99")).thenReturn(Maybe.empty());

        useCase.findById("99").test().assertError(NotFoundException.class);
    }

    @Test
    void listsAllProductsOfTheBank() {
        CreditCard product = new CreditCard(
                    new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD),
                    new BigDecimal("1000"));
        when(creditCardRepository.findAll()).thenReturn(Flowable.just(product));

        useCase.findAll().test().assertValues(product);
    }
}
