package dev.bootcamp.creditservice.application.port;

import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;

public interface CreditCardRepository {
    CreditCard save(CreditCard creditCard);
}
