package dev.bootcamp.creditservice.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
@DataMongoTest
@Import({CreditPersistenceAdapter.class, CreditCardPersistenceAdapter.class})
class CreditPersistenceAdaptersTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    private static final Customer PERSONAL = new Customer("c1", CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer("c2", CustomerType.BUSINESS);

    @Autowired private CreditPersistenceAdapter creditAdapter;

    @Autowired private CreditCardPersistenceAdapter creditCardAdapter;

    @Autowired private CreditMongoRepository creditMongoRepository;

    @Autowired private CreditCardMongoRepository creditCardMongoRepository;

    @BeforeEach
    void cleanCollections() {
        creditMongoRepository.deleteAll().block();
        creditCardMongoRepository.deleteAll().block();
    }

    @Test
    void savesAndFindsCredit() {
        Credit saved = creditAdapter.save(new Credit(PERSONAL, CreditType.PERSONAL)).blockingGet();

        Credit found = creditAdapter.findById(saved.getId()).blockingGet();
        assertThat(found.getType()).isEqualTo(CreditType.PERSONAL);
        assertThat(found.getCustomer()).isEqualTo(PERSONAL);
    }

    @Test
    void detectsExistingPersonalCreditOfCustomer() {
        creditAdapter.save(new Credit(PERSONAL, CreditType.PERSONAL)).blockingGet();

        creditAdapter
                .existsByCustomerIdAndType("c1", CreditType.PERSONAL)
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(true);
        creditAdapter
                .existsByCustomerIdAndType("c2", CreditType.PERSONAL)
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(false);
    }

    @Test
    void findsCreditsByCustomer() {
        creditAdapter.save(new Credit(BUSINESS, CreditType.BUSINESS)).blockingGet();
        creditAdapter.save(new Credit(BUSINESS, CreditType.BUSINESS)).blockingGet();
        creditAdapter.save(new Credit(PERSONAL, CreditType.PERSONAL)).blockingGet();

        creditAdapter
                .findByCustomerId("c2")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValueCount(2);
    }

    @Test
    void savesAndFindsCreditCardWithLimit() {
        CreditCard saved =
                creditCardAdapter
                        .save(new CreditCard(BUSINESS, new BigDecimal("5000.75")))
                        .blockingGet();

        CreditCard found = creditCardAdapter.findById(saved.getId()).blockingGet();
        assertThat(found.getCreditLimit()).isEqualByComparingTo("5000.75");
        assertThat(found.getCustomer()).isEqualTo(BUSINESS);
        creditCardAdapter
                .findByCustomerId("c2")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValueCount(1);
    }

    @Test
    void returnsEmptyWhenProductDoesNotExist() {
        creditAdapter
                .findById("000000000000000000000000")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
        creditCardAdapter
                .findById("000000000000000000000000")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
    }
}
