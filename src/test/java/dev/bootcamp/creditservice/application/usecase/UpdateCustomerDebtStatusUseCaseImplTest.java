package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerDebtStatusUseCaseImplTest {

    @Mock private CustomerDebtStatusRepository customerDebtStatusRepository;

    @InjectMocks private UpdateCustomerDebtStatusUseCaseImpl useCase;

    @Test
    void savesTheStatus() {
        CustomerDebtStatus status =
                new CustomerDebtStatus(
                        "1",
                        true,
                        BigDecimal.TEN,
                        Instant.parse("2026-11-15T10:00:00Z"));
        when(customerDebtStatusRepository.save(status)).thenReturn(Completable.complete());

        useCase.execute(status).test().assertComplete();

        verify(customerDebtStatusRepository).save(status);
    }
}
