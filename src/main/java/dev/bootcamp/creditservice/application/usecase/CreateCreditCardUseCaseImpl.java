package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

public class CreateCreditCardUseCaseImpl implements CreateCreditCardUseCase {
    private final CreditCardRepository creditCardRepository;
    private final CustomerClient customerClient;

    public CreateCreditCardUseCaseImpl(CreditCardRepository creditCardRepository, CustomerClient customerClient) {
        this.creditCardRepository = creditCardRepository;
        this.customerClient = customerClient;
    }

    @Override
    public Single<CreditCard> execute(String customerId, BigDecimal creditLimit) {
        return customerClient.findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")))
                .map(customer -> new CreditCard(customer, creditLimit))
                .flatMap(creditCardRepository::save);
    }
}
