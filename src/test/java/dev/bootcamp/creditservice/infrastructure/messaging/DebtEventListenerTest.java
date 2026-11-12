package dev.bootcamp.creditservice.infrastructure.messaging;

import dev.bootcamp.creditservice.application.usecase.UpdateCustomerDebtStatusUseCase;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtEventListenerTest {

    @Mock private UpdateCustomerDebtStatusUseCase updateCustomerDebtStatusUseCase;

    private DebtEventListener listener;

    @BeforeEach
    void createListener() {
        listener = new DebtEventListener(
                updateCustomerDebtStatusUseCase,
                JsonMapper.builder().build());
    }

    @Test
    void savesStatusOfCustomerDebtStatusChanged() {
        when(updateCustomerDebtStatusUseCase.execute(any())).thenReturn(Completable.complete());

        listener.onDebtEvent("{\"eventType\":\"CustomerDebtStatusChanged\",\"customerId\":\"c1\","
                + "\"hasOverdueDebt\":true,\"overdueAmount\":150.5,"
                + "\"occurredAt\":\"2026-11-15T10:00:00Z\"}");

        verify(updateCustomerDebtStatusUseCase).execute(new CustomerDebtStatus(
                "c1",
                true,
                new BigDecimal("150.5"),
                Instant.parse("2026-11-15T10:00:00Z")));
    }

    @Test
    void ignoresUnknownEventTypes() {
        listener.onDebtEvent("{\"eventType\":\"Other\",\"customerId\":\"c1\"}");

        verify(updateCustomerDebtStatusUseCase, never()).execute(any());
    }
}
