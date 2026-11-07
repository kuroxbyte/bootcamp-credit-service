package dev.bootcamp.creditservice.infrastructure.web.controller;

import dev.bootcamp.creditservice.application.usecase.CreateCreditCardUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditCardsUseCase;
import dev.bootcamp.creditservice.infrastructure.web.api.CreditCardsApi;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardRequestDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardResponseDto;
import dev.bootcamp.creditservice.infrastructure.web.mapper.CreditWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditCardController implements CreditCardsApi {

    private final CreateCreditCardUseCase createCreditCardUseCase;
    private final GetCreditCardsUseCase getCreditCardsUseCase;

    @Override
    public Mono<ResponseEntity<CreditCardResponseDto>> createCreditCard(String xCustomerId,
                                                                        Mono<CreditCardRequestDto> creditCardRequestDto,
                                                                        ServerWebExchange exchange) {
        return creditCardRequestDto
                .flatMap(request -> Mono.from(createCreditCardUseCase
                        .execute(xCustomerId, request.getCreditLimit())
                        .toFlowable()))
                .doOnNext(card -> log.info("Credit card granted creditCardId={} customerId={}",
                        card.getId(), xCustomerId))
                .map(card -> ResponseEntity.status(HttpStatus.CREATED).body(CreditWebMapper.toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditCardResponseDto>>> listCreditCards(String xCustomerId,
                                                                             ServerWebExchange exchange) {
        log.debug("Listing credit cards customerId={}", xCustomerId);
        Flux<CreditCardResponseDto> cards = Flux.from(getCreditCardsUseCase.findByCustomerId(xCustomerId))
                .map(CreditWebMapper::toResponse);
        return Mono.just(ResponseEntity.ok(cards));
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponseDto>> getCreditCardById(String creditCardId,
                                                                         ServerWebExchange exchange) {
        log.debug("Fetching credit card creditCardId={}", creditCardId);
        return Mono.from(getCreditCardsUseCase.findById(creditCardId).toFlowable())
                .map(card -> ResponseEntity.ok(CreditWebMapper.toResponse(card)));
    }
}
