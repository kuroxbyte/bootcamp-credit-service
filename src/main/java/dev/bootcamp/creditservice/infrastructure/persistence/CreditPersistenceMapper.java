package dev.bootcamp.creditservice.infrastructure.persistence;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;

/**
 * Conversión entre el dominio y los documentos de MongoDB.
 */
final class CreditPersistenceMapper {
    private CreditPersistenceMapper() {}

    static CreditDocument toDocument(Credit credit) {
        return new CreditDocument(
                credit.getId(),
                credit.getCustomer().id(),
                credit.getCustomer().type().name(),
                credit.getCustomer().profile().name(),
                credit.getType().name(),
                credit.getAmount(),
                credit.getDueDate(),
                credit.getGrantedAt(),
                credit.getClosedAt());
    }

    static CreditCardDocument toDocument(CreditCard creditCard) {
        return new CreditCardDocument(
                creditCard.getId(),
                creditCard.getCustomer().id(),
                creditCard.getCustomer().type().name(),
                creditCard.getCustomer().profile().name(),
                creditCard.getCreditLimit(),
                creditCard.getPaymentDueDay(),
                creditCard.getGrantedAt(),
                creditCard.getClosedAt());
    }

    static Credit toDomain(CreditDocument document) {
        Credit credit =
                new Credit(
                        customer(
                                document.getCustomerId(),
                                document.getCustomerType(),
                                document.getCustomerProfile()),
                        CreditType.valueOf(document.getType()),
                        document.getAmount(),
                        document.getDueDate());
        credit.setId(document.getId());
        credit.setGrantedAt(document.getGrantedAt());
        credit.setClosedAt(document.getClosedAt());
        return credit;
    }

    static CreditCard toDomain(CreditCardDocument document) {
        CreditCard creditCard =
                new CreditCard(
                        customer(
                                document.getCustomerId(),
                                document.getCustomerType(),
                                document.getCustomerProfile()),
                        document.getCreditLimit(),
                        document.getPaymentDueDay());
        creditCard.setId(document.getId());
        creditCard.setGrantedAt(document.getGrantedAt());
        creditCard.setClosedAt(document.getClosedAt());
        return creditCard;
    }

    private static Customer customer(String id, String type, String profile) {
        return new Customer(id, CustomerType.valueOf(type), CustomerProfile.valueOf(profile));
    }
}
