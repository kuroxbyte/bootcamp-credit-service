package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import java.math.BigDecimal;

public class CreateCreditCardUseCaseImpl implements CreateCreditCardUseCase {
    private final CreditCardRepository creditCardRepository;
    private final CustomerClient customerClient;

    public CreateCreditCardUseCaseImpl(CreditCardRepository creditCardRepository, CustomerClient customerClient) {
        this.creditCardRepository = creditCardRepository;
        this.customerClient = customerClient;
    }

    @Override
    public CreditCard execute(Long customerId, BigDecimal creditLimit) {
        Customer customer = customerClient.findById(customerId)
                .orElseThrow(() -> new BusinessRuleException("El cliente no existe"));
        return creditCardRepository.save(new CreditCard(customer, creditLimit));
    }
}
