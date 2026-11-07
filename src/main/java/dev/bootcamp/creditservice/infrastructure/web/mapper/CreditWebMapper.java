package dev.bootcamp.creditservice.infrastructure.web.mapper;

import dev.bootcamp.creditservice.domain.model.credit.Credit;
import dev.bootcamp.creditservice.domain.model.credit.CreditType;
import dev.bootcamp.creditservice.domain.model.creditcard.CreditCard;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditCardResponseDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditResponseDto;
import dev.bootcamp.creditservice.infrastructure.web.dto.CreditTypeDto;

/** Conversión entre el dominio y los DTOs del contrato OpenAPI. */
public final class CreditWebMapper {

    private CreditWebMapper() {
    }

    public static CreditType toDomain(CreditTypeDto type) {
        return CreditType.valueOf(type.name());
    }

    public static CreditResponseDto toResponse(Credit credit) {
        return new CreditResponseDto(
                credit.getId(), credit.getCustomer().id(), CreditTypeDto.valueOf(credit.getType().name()));
    }

    public static CreditCardResponseDto toResponse(CreditCard creditCard) {
        return new CreditCardResponseDto(
                creditCard.getId(), creditCard.getCustomer().id(), creditCard.getCreditLimit());
    }
}
