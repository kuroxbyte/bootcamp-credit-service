package dev.bootcamp.creditservice.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.bootcamp.creditservice.application.usecase.CreateCreditUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditsUseCase;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(CreditController.class)
class CreditControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateCreditUseCase createCreditUseCase;

    @MockitoBean
    private GetCreditsUseCase getCreditsUseCase;

    private static Credit personalCredit() {
        Credit credit = new Credit(new Customer("1", CustomerType.PERSONAL), CreditType.PERSONAL);
        credit.setId("5");
        return credit;
    }

    @Test
    void grantsCredit() {
        when(createCreditUseCase.execute("1", CreditType.PERSONAL)).thenReturn(Single.just(personalCredit()));

        webTestClient.post().uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"type\":\"PERSONAL\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("5")
                .jsonPath("$.customerId").isEqualTo("1")
                .jsonPath("$.type").isEqualTo("PERSONAL");
    }

    @Test
    void rejectsUnknownCreditType() {
        webTestClient.post().uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"type\":\"CREDIT_CARD\"}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(createCreditUseCase, never()).execute(any(), any());
    }

    @Test
    void returnsUnprocessableForSecondPersonalCredit() {
        when(createCreditUseCase.execute("1", CreditType.PERSONAL))
                .thenReturn(Single.error(new BusinessRuleException("El cliente ya tiene un crédito personal")));

        webTestClient.post().uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"type\":\"PERSONAL\"}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void returnsNotFoundWhenCustomerDoesNotExist() {
        when(createCreditUseCase.execute("99", CreditType.PERSONAL))
                .thenReturn(Single.error(new NotFoundException("El cliente no existe")));

        webTestClient.post().uri("/api/v1/credits")
                .header("X-Customer-Id", "99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"type\":\"PERSONAL\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listsCreditsOfCustomer() {
        when(getCreditsUseCase.findByCustomerId("1")).thenReturn(Flowable.just(personalCredit()));

        webTestClient.get().uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("5");
    }

    @Test
    void returnsCreditById() {
        when(getCreditsUseCase.findById("5")).thenReturn(Single.just(personalCredit()));

        webTestClient.get().uri("/api/v1/credits/5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo("1");
    }
}
