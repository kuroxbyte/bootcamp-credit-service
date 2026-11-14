package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditDebtClient;
import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link UpdateCreditUseCase}.
 */
@RequiredArgsConstructor
public class UpdateCreditUseCaseImpl implements UpdateCreditUseCase {
    private final CreditRepository creditRepository;
    private final CreditDebtClient creditDebtClient;
    private final CreditEventPublisher creditEventPublisher;
    private final Clock clock;

    @Override
    public Single<Credit> update(String creditId, LocalDate dueDate) {
        return GetCreditsUseCaseImpl.findActive(creditRepository, creditId)
                .map(credit -> credit.withDueDate(dueDate))
                .flatMap(creditRepository::save)
                .flatMap(saved -> creditEventPublisher.publishCreditUpdated(saved)
                        .andThen(Single.just(saved)));
    }

    @Override
    public Completable close(String creditId) {
        return GetCreditsUseCaseImpl.findActive(creditRepository, creditId)
                .flatMap(credit -> creditDebtClient
                        .paidAmount(creditId, credit.getCustomer().id())
                        .flatMap(paid -> {
                            BigDecimal debt = credit.getAmount().subtract(paid);
                            if (debt.signum() > 0) {
                                return Single.<Credit>error(new BusinessRuleException(
                                        "El crédito tiene una deuda de " + debt.toPlainString()
                                                + "; debe pagarse por completo para darlo de"
                                                + " baja"));
                            }
                            credit.close(LocalDate.now(clock));
                            return creditRepository.save(credit);
                        }))
                .flatMapCompletable(creditEventPublisher::publishCreditClosed);
    }
}
