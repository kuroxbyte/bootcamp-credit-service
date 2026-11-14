package dev.bootcamp.creditservice.infrastructure.web.controller;

import dev.bootcamp.creditservice.application.usecase.CreateCreditUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditsUseCase;
import dev.bootcamp.creditservice.application.usecase.UpdateCreditUseCase;
import dev.bootcamp.creditservice.domain.exception.BusinessRuleException;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
import dev.bootcamp.creditservice.infrastructure.client.ServiceUnavailableException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CreditController.class)
class CreditControllerTest {

    private static final String PERSONAL_CREDIT_REQUEST =
            "{\"type\":\"PERSONAL\",\"amount\":10,\"dueDate\":\"2027-01-15\"}";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private CreateCreditUseCase createCreditUseCase;

    @MockitoBean private GetCreditsUseCase getCreditsUseCase;

    @MockitoBean private UpdateCreditUseCase updateCreditUseCase;

    private static Credit personalCredit() {
        Credit credit = new Credit(
                new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD),
                CreditType.PERSONAL,
                BigDecimal.TEN,
                LocalDate.of(2027, 1, 15));
        credit.setId("5");
        credit.setGrantedAt(LocalDate.of(2026, 11, 15));
        return credit;
    }

    @Test
    void grantsCredit() {
        when(
                createCreditUseCase.execute(
                        "1",
                        CreditType.PERSONAL,
                        BigDecimal.TEN,
                        LocalDate.of(2027, 1, 15)))
                .thenReturn(Single.just(personalCredit()));

        webTestClient
                .post()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(PERSONAL_CREDIT_REQUEST)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo("5")
                .jsonPath("$.customerId")
                .isEqualTo("1")
                .jsonPath("$.type")
                .isEqualTo("PERSONAL");
    }

    @Test
    void rejectsUnknownCreditType() {
        webTestClient
                .post()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"type\":\"CREDIT_CARD\",\"amount\":10,\"dueDate\":\"2027-01-15\"}")
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(createCreditUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void returnsUnprocessableForSecondPersonalCredit() {
        when(
                createCreditUseCase.execute(
                        "1",
                        CreditType.PERSONAL,
                        BigDecimal.TEN,
                        LocalDate.of(2027, 1, 15)))
                .thenReturn(
                        Single.error(
                                new BusinessRuleException(
                                        "El cliente ya tiene un crédito personal")));

        webTestClient
                .post()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(PERSONAL_CREDIT_REQUEST)
                .exchange()
                .expectStatus()
                .isEqualTo(422)
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void returnsNotFoundWhenCustomerDoesNotExist() {
        when(
                createCreditUseCase.execute(
                        "99",
                        CreditType.PERSONAL,
                        BigDecimal.TEN,
                        LocalDate.of(2027, 1, 15)))
                .thenReturn(Single.error(new NotFoundException("El cliente no existe")));

        webTestClient
                .post()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(PERSONAL_CREDIT_REQUEST)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void listsCreditsOfCustomer() {
        when(getCreditsUseCase.findByCustomerId("1")).thenReturn(Flowable.just(personalCredit()));

        webTestClient
                .get()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].id")
                .isEqualTo("5");
    }

    @Test
    void returnsCreditById() {
        when(getCreditsUseCase.findById("5")).thenReturn(Single.just(personalCredit()));

        webTestClient
                .get()
                .uri("/api/v1/credits/5")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.customerId")
                .isEqualTo("1");
    }

    @Test
    void returnsServiceUnavailableWhenCustomerServiceIsDown() {
        when(
                createCreditUseCase.execute(
                        "1",
                        CreditType.PERSONAL,
                        BigDecimal.TEN,
                        LocalDate.of(2027, 1, 15)))
                .thenReturn(
                        Single.error(
                                new ServiceUnavailableException(
                                        "customer-service no está disponible",
                                        new RuntimeException())));

        webTestClient
                .post()
                .uri("/api/v1/credits")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(PERSONAL_CREDIT_REQUEST)
                .exchange()
                .expectStatus()
                .isEqualTo(503);
    }

    @Test
    void listsAllForReportsWithGrantedDate() {
        when(getCreditsUseCase.findAll()).thenReturn(Flowable.just(personalCredit()));

        webTestClient
                .get()
                .uri("/api/v1/internal/credits")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].grantedAt")
                .isEqualTo("2026-11-15");
    }

    @Test
    void refinancesCreditForAdmin() {
        when(updateCreditUseCase.update("5", LocalDate.of(2028, 5, 15)))
                .thenReturn(Single.just(personalCredit()));

        webTestClient.put().uri("/api/v1/credits/5")
                .header("X-Admin-Id", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"dueDate\":\"2028-05-15\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo("5");
    }

    @Test
    void closesCreditOrExplainsWhyNot() {
        when(updateCreditUseCase.close("5")).thenReturn(Completable.complete());
        when(updateCreditUseCase.close("6"))
                .thenReturn(Completable.error(new BusinessRuleException("Tiene deuda")));

        webTestClient.delete().uri("/api/v1/credits/5").header("X-Admin-Id", "admin")
                .exchange().expectStatus().isNoContent();
        webTestClient.delete().uri("/api/v1/credits/6").header("X-Admin-Id", "admin")
                .exchange().expectStatus().isEqualTo(422);
    }

    @Test
    void showsClosingDateInTheInternalList() {
        Credit closed = personalCredit();
        closed.close(LocalDate.of(2027, 1, 10));
        when(getCreditsUseCase.findAll()).thenReturn(Flowable.just(closed));

        webTestClient.get().uri("/api/v1/internal/credits").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].closedAt").isEqualTo("2027-01-10");
    }
}
