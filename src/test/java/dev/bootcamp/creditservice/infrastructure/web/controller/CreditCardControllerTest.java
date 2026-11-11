package dev.bootcamp.creditservice.infrastructure.web.controller;

import dev.bootcamp.creditservice.application.usecase.CreateCreditCardUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditCardsUseCase;
import dev.bootcamp.creditservice.domain.exception.NotFoundException;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.domain.model.customer.Customer;
import dev.bootcamp.creditservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.creditservice.domain.model.customer.CustomerType;
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

@WebFluxTest(CreditCardController.class)
class CreditCardControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private CreateCreditCardUseCase createCreditCardUseCase;

    @MockitoBean private GetCreditCardsUseCase getCreditCardsUseCase;

    private static CreditCard card() {
        CreditCard card =
                new CreditCard(
                        new Customer("1", CustomerType.PERSONAL, CustomerProfile.STANDARD),
                        new BigDecimal("5000"));
        card.setId("7");
        card.setGrantedAt(LocalDate.of(2026, 11, 15));
        return card;
    }

    @Test
    void grantsCreditCard() {
        when(createCreditCardUseCase.execute("1", new BigDecimal("5000")))
                .thenReturn(Single.just(card()));

        webTestClient
                .post()
                .uri("/api/v1/credit-cards")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"creditLimit\":5000}")
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo("7")
                .jsonPath("$.creditLimit")
                .isEqualTo(5000);
    }

    @Test
    void rejectsNonPositiveCreditLimit() {
        webTestClient
                .post()
                .uri("/api/v1/credit-cards")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"creditLimit\":0}")
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(createCreditCardUseCase, never()).execute(any(), any());
    }

    @Test
    void listsCardsOfCustomer() {
        when(getCreditCardsUseCase.findByCustomerId("1")).thenReturn(Flowable.just(card()));

        webTestClient
                .get()
                .uri("/api/v1/credit-cards")
                .header("X-Customer-Id", "1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].id")
                .isEqualTo("7");
    }

    @Test
    void returnsNotFoundWhenCardDoesNotExist() {
        when(getCreditCardsUseCase.findById("99"))
                .thenReturn(Single.error(new NotFoundException("La tarjeta de crédito no existe")));

        webTestClient.get().uri("/api/v1/credit-cards/99").exchange().expectStatus().isNotFound();
    }

    @Test
    void listsAllForReportsWithGrantedDate() {
        when(getCreditCardsUseCase.findAll()).thenReturn(Flowable.just(card()));

        webTestClient
                .get()
                .uri("/api/v1/internal/credit-cards")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].grantedAt")
                .isEqualTo("2026-11-15");
    }
}
