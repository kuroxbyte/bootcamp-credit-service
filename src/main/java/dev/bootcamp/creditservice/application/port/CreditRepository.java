package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface CreditRepository {
    Single<Boolean> existsByCustomerIdAndType(String customerId, CreditType type);

    /**
     * @return el crédito, o vacío si no existe
     */
    Maybe<Credit> findById(String creditId);

    /**
     * @return los créditos del cliente (ninguno si no tiene)
     */
    Flowable<Credit> findByCustomerId(String customerId);

    Single<Credit> save(Credit credit);
}
