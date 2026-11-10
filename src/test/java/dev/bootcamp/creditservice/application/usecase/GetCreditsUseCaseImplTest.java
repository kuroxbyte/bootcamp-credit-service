package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCreditsUseCaseImplTest {

    private static final Credit CREDIT =
            new Credit(
                    new Customer("2", CustomerType.BUSINESS, CustomerProfile.STANDARD),
                    CreditType.BUSINESS);

    @Mock private CreditRepository creditRepository;

    @InjectMocks private GetCreditsUseCaseImpl useCase;

    @Test
    void listsCreditsOfCustomer() {
        when(creditRepository.findByCustomerId("2")).thenReturn(Flowable.just(CREDIT));

        useCase.findByCustomerId("2").test().assertValues(CREDIT);
    }

    @Test
    void returnsExistingCredit() {
        when(creditRepository.findById("5")).thenReturn(Maybe.just(CREDIT));

        useCase.findById("5").test().assertValue(CREDIT);
    }

    @Test
    void failsWhenCreditDoesNotExist() {
        when(creditRepository.findById("99")).thenReturn(Maybe.empty());

        useCase.findById("99").test().assertError(NotFoundException.class);
    }
}
