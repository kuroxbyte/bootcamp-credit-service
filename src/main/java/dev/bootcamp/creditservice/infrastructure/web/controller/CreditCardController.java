package dev.bootcamp.creditservice.infrastructure.web.controller;

import dev.bootcamp.creditservice.application.usecase.CreateCreditCardUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditCardsUseCase;
import dev.bootcamp.creditservice.application.usecase.UpdateCreditCardUseCase;
import dev.bootcamp.creditservice.infrastructure.web.api.CreditCardsApi;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardRequestDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardResponseDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardUpdateRequestDto;
import dev.bootcamp.creditservice.infrastructure.web.mapper.CreditWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints de tarjetas de crédito definidos en el contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditCardController implements CreditCardsApi {

    private final CreateCreditCardUseCase createCreditCardUseCase;
    private final GetCreditCardsUseCase getCreditCardsUseCase;
    private final UpdateCreditCardUseCase updateCreditCardUseCase;

    @Override
    public Mono<ResponseEntity<CreditCardResponseDto>> createCreditCard(
            String customerId,
            Mono<CreditCardRequestDto> creditCardRequestDto,
            ServerWebExchange exchange) {
        return creditCardRequestDto
                .flatMap(
                        request ->
                                Mono.from(
                                        createCreditCardUseCase
                                                .execute(
                                                        customerId,
                                                        request.getCreditLimit(),
                                                        request.getPaymentDueDay())
                                                .toFlowable()))
                .doOnNext(
                        card ->
                                log.info(
                                        "Credit card granted creditCardId={} customerId={}",
                                        card.getId(),
                                        customerId))
                .map(
                        card ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(CreditWebMapper.toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditCardResponseDto>>> listAllCreditCards(
            ServerWebExchange exchange) {
        log.debug("Listing all credit cards for reports");
        return Mono.just(ResponseEntity.ok(
                Flux.from(getCreditCardsUseCase.findAll()).map(CreditWebMapper::toResponse)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditCardResponseDto>>> listCreditCards(
            String customerId,
            ServerWebExchange exchange) {
        log.debug("Listing credit cards customerId={}", customerId);
        Flux<CreditCardResponseDto> cards =
                Flux.from(getCreditCardsUseCase.findByCustomerId(customerId))
                        .map(CreditWebMapper::toResponse);
        return Mono.just(ResponseEntity.ok(cards));
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponseDto>> getCreditCardById(
            String creditCardId,
            ServerWebExchange exchange) {
        log.debug("Fetching credit card creditCardId={}", creditCardId);
        return Mono.from(getCreditCardsUseCase.findById(creditCardId).toFlowable())
                .map(card -> ResponseEntity.ok(CreditWebMapper.toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponseDto>> updateCreditCard(
            String creditCardId,
            String adminId,
            Mono<CreditCardUpdateRequestDto> creditCardUpdateRequestDto,
            ServerWebExchange exchange) {
        return creditCardUpdateRequestDto
                .flatMap(request -> Mono.from(updateCreditCardUseCase
                        .update(
                                creditCardId,
                                request.getCreditLimit(),
                                request.getPaymentDueDay())
                        .toFlowable()))
                .doOnNext(card -> log.info(
                        "Credit card updated creditCardId={} creditLimit={} adminId={}",
                        creditCardId,
                        card.getCreditLimit(),
                        adminId))
                .map(card -> ResponseEntity.ok(CreditWebMapper.toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCreditCard(
            String creditCardId,
            String adminId,
            ServerWebExchange exchange) {
        return Mono.from(updateCreditCardUseCase.close(creditCardId).toFlowable())
                .then(Mono.fromCallable(() -> {
                    log.info(
                            "Credit card closed creditCardId={} adminId={}",
                            creditCardId,
                            adminId);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }
}
