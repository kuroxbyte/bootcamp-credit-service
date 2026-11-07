package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Single;

public interface CreditCardRepository {
    Single<CreditCard> save(CreditCard creditCard);
}
