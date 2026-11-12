package dev.bootcamp.creditservice.application.rule;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueDebtRuleTest {

    @Mock private CustomerDebtStatusRepository customerDebtStatusRepository;

    @InjectMocks private OverdueDebtRule rule;

    @Test
    void passesWhenCustomerHasNoOverdueDebt() {
        when(customerDebtStatusRepository.hasOverdueDebt("1")).thenReturn(Single.just(false));

        rule.verify("1").test().assertComplete();
    }

    @Test
    void rejectsCustomerWithOverdueDebt() {
        when(customerDebtStatusRepository.hasOverdueDebt("1")).thenReturn(Single.just(true));

        rule.verify("1").test().assertError(error -> error instanceof BusinessRuleException
                && error.getMessage().equals(OverdueDebtRule.MESSAGE));
    }
}
