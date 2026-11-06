package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;

public interface CreditRepository {
    boolean existsByCustomerIdAndType(String customerId, CreditType type);

    Credit save(Credit credit);
}
