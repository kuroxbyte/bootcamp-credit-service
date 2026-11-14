package dev.bootcamp.creditservice.infrastructure.client.transaction;

import dev.bootcamp.creditservice.application.port.CreditDebtClient;
import dev.bootcamp.creditservice.infrastructure.client.RemoteLookup;
import dev.bootcamp.creditservice.infrastructure.client.ServiceUnavailableException;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/**
 * Implementa el puerto {@link CreditDebtClient} con transaction-service, protegido con un circuit
 * breaker.
 *
 * <ul>
 *   <li>Pagado de un crédito: suma de los movimientos {@code PAYMENT} de
 *       {@code GET /api/v1/credits/{creditId}/movements}.
 *   <li>Disponible de una tarjeta:
 *       {@code GET /api/v1/credit-cards/{creditCardId}/available-credit}.
 * </ul>
 */
@Component
public class CreditDebtWebClientAdapter implements CreditDebtClient {

    private static final String SERVICE_NAME = "transaction-service";
    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";
    private static final String PAYMENT = "PAYMENT";

    private final WebClient webClient;
    private final RemoteLookup remoteLookup;

    /**
     * Crea el cliente con la URL base configurada.
     */
    public CreditDebtWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${clients.transaction-service.base-url}") String baseUrl,
            RemoteLookup remoteLookup) {
        this.webClient = webClientBuilder.clone().baseUrl(baseUrl).build();
        this.remoteLookup = remoteLookup;
    }

    @Override
    public Single<BigDecimal> paidAmount(String creditId, String customerId) {
        return orUnavailable(remoteLookup.findById(
                webClient.get()
                        .uri("/api/v1/credits/{creditId}/movements", creditId)
                        .header(CUSTOMER_ID_HEADER, customerId)
                        .retrieve()
                        .bodyToFlux(ProductMovement.class)
                        .filter(movement -> PAYMENT.equals(movement.type()))
                        .map(ProductMovement::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                SERVICE_NAME,
                creditId), creditId);
    }

    @Override
    public Single<BigDecimal> availableCredit(String creditCardId, String customerId) {
        return orUnavailable(remoteLookup.findById(
                webClient.get()
                        .uri("/api/v1/credit-cards/{creditCardId}/available-credit", creditCardId)
                        .header(CUSTOMER_ID_HEADER, customerId)
                        .retrieve()
                        .bodyToMono(AvailableCredit.class)
                        .map(AvailableCredit::availableCredit),
                SERVICE_NAME,
                creditCardId), creditCardId);
    }

    private static Single<BigDecimal> orUnavailable(
            Maybe<BigDecimal> value,
            String productId) {
        return value.switchIfEmpty(Single.error(() -> new ServiceUnavailableException(
                SERVICE_NAME + " no conoce el producto " + productId, null)));
    }

    /**
     * Movimiento de un producto ({@code ProductMovementResponse} del contrato de
     * transaction-service).
     *
     * @param type   PAYMENT para los pagos
     * @param amount monto del movimiento
     */
    record ProductMovement(String type, BigDecimal amount) {}

    /**
     * Crédito disponible ({@code AvailableCreditResponse} del contrato de transaction-service).
     *
     * @param creditCardId   tarjeta consultada
     * @param availableCredit crédito disponible
     */
    record AvailableCredit(String creditCardId, BigDecimal availableCredit) {}
}
