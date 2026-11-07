package dev.bootcamp.creditservice.infrastructure.persistence;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;

/** Conversión entre el dominio y los documentos de MongoDB. */
final class CreditPersistenceMapper {

    private CreditPersistenceMapper() {
    }

    static CreditDocument toDocument(Credit credit) {
        return new CreditDocument(credit.getId(), credit.getCustomer().id(),
                credit.getCustomer().type().name(), credit.getType().name());
    }

    static Credit toDomain(CreditDocument document) {
        Credit credit = new Credit(customer(document.getCustomerId(), document.getCustomerType()),
                CreditType.valueOf(document.getType()));
        credit.setId(document.getId());
        return credit;
    }

    static CreditCardDocument toDocument(CreditCard creditCard) {
        return new CreditCardDocument(creditCard.getId(), creditCard.getCustomer().id(),
                creditCard.getCustomer().type().name(), creditCard.getCreditLimit());
    }

    static CreditCard toDomain(CreditCardDocument document) {
        CreditCard creditCard = new CreditCard(customer(document.getCustomerId(), document.getCustomerType()),
                document.getCreditLimit());
        creditCard.setId(document.getId());
        return creditCard;
    }

    private static Customer customer(String id, String type) {
        return new Customer(id, CustomerType.valueOf(type));
    }
}
