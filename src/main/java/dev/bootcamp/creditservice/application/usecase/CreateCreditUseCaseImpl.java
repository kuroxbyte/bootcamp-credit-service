package dev.bootcamp.creditservice.application.usecase;

import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.customer.Customer;

public class CreateCreditUseCaseImpl implements CreateCreditUseCase {
    private final CreditRepository creditRepository;
    private final CustomerClient customerClient;

    public CreateCreditUseCaseImpl(CreditRepository creditRepository, CustomerClient customerClient) {
        this.creditRepository = creditRepository;
        this.customerClient = customerClient;
    }

    @Override
    public Credit execute(Long customerId, CreditType type) {
        Customer customer = customerClient.findById(customerId)
                .orElseThrow(() -> new BusinessRuleException("El cliente no existe"));
        Credit credit = new Credit(customer, type);
        // Personal: solo un crédito por persona. Empresarial: sin límite.
        if (type == CreditType.PERSONAL
                && creditRepository.existsByCustomerIdAndType(customerId, CreditType.PERSONAL)) {
            throw new BusinessRuleException("El cliente ya tiene un crédito personal");
        }
        return creditRepository.save(credit);
    }
}
