package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Single;

public interface CreditRepository {
    Single<Boolean> existsByCustomerIdAndType(String customerId, CreditType type);

    Single<Credit> save(Credit credit);
}
