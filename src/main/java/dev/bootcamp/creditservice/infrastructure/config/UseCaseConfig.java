package dev.bootcamp.creditservice.infrastructure.config;

import dev.bootcamp.creditservice.application.port.CreditCardRepository;
import dev.bootcamp.creditservice.application.port.CreditRepository;
import dev.bootcamp.creditservice.application.port.CustomerClient;
import dev.bootcamp.creditservice.application.usecase.CreateCreditCardUseCase;
import dev.bootcamp.creditservice.application.usecase.CreateCreditCardUseCaseImpl;
import dev.bootcamp.creditservice.application.usecase.CreateCreditUseCase;
import dev.bootcamp.creditservice.application.usecase.CreateCreditUseCaseImpl;
import dev.bootcamp.creditservice.application.usecase.GetCreditCardsUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditCardsUseCaseImpl;
import dev.bootcamp.creditservice.application.usecase.GetCreditsUseCase;
import dev.bootcamp.creditservice.application.usecase.GetCreditsUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring;
 * aquí se les inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateCreditUseCase createCreditUseCase(CreditRepository creditRepository, CustomerClient customerClient) {
        return new CreateCreditUseCaseImpl(creditRepository, customerClient);
    }

    @Bean
    public GetCreditsUseCase getCreditsUseCase(CreditRepository creditRepository) {
        return new GetCreditsUseCaseImpl(creditRepository);
    }

    @Bean
    public CreateCreditCardUseCase createCreditCardUseCase(CreditCardRepository creditCardRepository,
                                                           CustomerClient customerClient) {
        return new CreateCreditCardUseCaseImpl(creditCardRepository, customerClient);
    }

    @Bean
    public GetCreditCardsUseCase getCreditCardsUseCase(CreditCardRepository creditCardRepository) {
        return new GetCreditCardsUseCaseImpl(creditCardRepository);
    }
}
