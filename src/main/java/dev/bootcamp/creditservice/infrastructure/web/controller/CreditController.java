package dev.bootcamp.creditservice.infrastructure.web.controller;

import dev.bootcamp.creditservice.application.usecase.CreateCreditUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditsUseCase;
import dev.bootcamp.creditservice.infrastructure.web.api.CreditsApi;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditRequestDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditResponseDto;
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
 * Endpoints de créditos personales y empresariales definidos en el contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditController implements CreditsApi {

    private final CreateCreditUseCase createCreditUseCase;
    private final GetCreditsUseCase getCreditsUseCase;

    @Override
    public Mono<ResponseEntity<CreditResponseDto>> createCredit(
            String customerId,
            Mono<CreditRequestDto> creditRequestDto,
            ServerWebExchange exchange) {
        return creditRequestDto
                .flatMap(
                        request ->
                                Mono.from(
                                        createCreditUseCase
                                                .execute(
                                                        customerId,
                                                        CreditWebMapper.toDomain(request.getType()))
                                                .toFlowable()))
                .doOnNext(
                        credit ->
                                log.info(
                                        "Credit granted creditId={} customerId={} type={}",
                                        credit.getId(),
                                        customerId,
                                        credit.getType()))
                .map(
                        credit ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(CreditWebMapper.toResponse(credit)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponseDto>>> listCredits(
            String customerId,
            ServerWebExchange exchange) {
        log.debug("Listing credits customerId={}", customerId);
        Flux<CreditResponseDto> credits =
                Flux.from(getCreditsUseCase.findByCustomerId(customerId))
                        .map(CreditWebMapper::toResponse);
        return Mono.just(ResponseEntity.ok(credits));
    }

    @Override
    public Mono<ResponseEntity<CreditResponseDto>> getCreditById(
            String creditId,
            ServerWebExchange exchange) {
        log.debug("Fetching credit creditId={}", creditId);
        return Mono.from(getCreditsUseCase.findById(creditId).toFlowable())
                .map(credit -> ResponseEntity.ok(CreditWebMapper.toResponse(credit)));
    }
}
