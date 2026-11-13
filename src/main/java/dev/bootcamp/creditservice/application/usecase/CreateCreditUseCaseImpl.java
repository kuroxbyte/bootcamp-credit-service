package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditEventPublisher;
import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.application.rule.OverdueDebtRule;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link CreateCreditUseCase}.
 */
@RequiredArgsConstructor
public class CreateCreditUseCaseImpl implements CreateCreditUseCase {
    private final CreditRepository creditRepository;
    private final CustomerClient customerClient;
    private final OverdueDebtRule overdueDebtRule;
    private final CreditEventPublisher creditEventPublisher;
    private final Clock clock;

    @Override
    public Single<Credit> execute(
            String customerId,
            CreditType type,
            BigDecimal amount,
            LocalDate dueDate) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .flatMap(customer -> overdueDebtRule.verify(customerId)
                        .andThen(Single.fromCallable(
                                () -> new Credit(customer, type, amount, dueDate))))
                .flatMap(this::validateOnePersonalCreditPerCustomer)
                .flatMap(credit -> {
                    credit.grant(LocalDate.now(clock));
                    return creditRepository.save(credit);
                })
                .flatMap(saved -> creditEventPublisher
                        .publishCreditGranted(saved)
                        .andThen(Single.just(saved)));
    }

    // Personal: solo un crédito por persona. Empresarial: sin límite.
    private Single<Credit> validateOnePersonalCreditPerCustomer(Credit credit) {
        if (credit.getType() != CreditType.PERSONAL) {
            return Single.just(credit);
        }
        return creditRepository
                .existsByCustomerIdAndType(credit.getCustomer().id(), CreditType.PERSONAL)
                .flatMap(
                        exists ->
                                exists
                                        ? Single.error(
                                                new BusinessRuleException(
                                                        "El cliente ya tiene un crédito personal"))
                                        : Single.just(credit));
    }
}
