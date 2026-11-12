package dev.bootcamp.creditservice.infrastructure.cache;

import dev.bootcamp.creditservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import dev.bootcamp.creditservice.infrastructure.persistence.CustomerDebtStatusDocument;
import dev.bootcamp.creditservice.infrastructure.persistence.CustomerDebtStatusMongoRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementa el puerto {@link CustomerDebtStatusRepository} con MongoDB como almacén y Redis como
 * caché (cache-aside) de la marca de deuda vencida.
 *
 * <ul>
 *   <li>Guardar: escribe en MongoDB y después en Redis ({@code debt-status:{customerId}}).
 *   <li>Consultar: lee de Redis; si no está, lee de MongoDB y lo deja en Redis.
 *   <li>Cada entrada vence según {@code cache.debt-status.ttl}.
 *   <li>Si Redis falla, se registra y se sigue con MongoDB: la caché solo acelera el acceso.
 * </ul>
 */
@Slf4j
@Component
public class CachedCustomerDebtStatusRepository implements CustomerDebtStatusRepository {

    private static final String KEY_PREFIX = "debt-status:";

    private final CustomerDebtStatusMongoRepository mongoRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final Duration ttl;

    /**
     * Crea el repositorio con el tiempo de vida configurado para la caché.
     */
    public CachedCustomerDebtStatusRepository(
            CustomerDebtStatusMongoRepository mongoRepository,
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${cache.debt-status.ttl}") Duration ttl) {
        this.mongoRepository = mongoRepository;
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    @Override
    public Completable save(CustomerDebtStatus status) {
        return Completable.fromPublisher(mongoRepository
                .save(new CustomerDebtStatusDocument(
                        status.customerId(),
                        status.hasOverdueDebt(),
                        status.overdueAmount(),
                        status.occurredAt()))
                .then(cache(status.customerId(), status.hasOverdueDebt())));
    }

    @Override
    public Single<Boolean> hasOverdueDebt(String customerId) {
        Mono<Boolean> fromDatabase = mongoRepository.findById(customerId)
                .map(CustomerDebtStatusDocument::isHasOverdueDebt)
                .defaultIfEmpty(false)
                .flatMap(overdue -> cache(customerId, overdue).thenReturn(overdue));
        return Single.fromPublisher(cached(customerId).switchIfEmpty(fromDatabase));
    }

    private Mono<Boolean> cached(String customerId) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + customerId)
                .map(Boolean::parseBoolean)
                .onErrorResume(error -> {
                    log.warn("Redis read failed customerId={}", customerId, error);
                    return Mono.empty();
                });
    }

    private Mono<Void> cache(String customerId, boolean overdue) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + customerId, Boolean.toString(overdue), ttl)
                .then()
                .onErrorResume(error -> {
                    log.warn("Redis write failed customerId={}", customerId, error);
                    return Mono.empty();
                });
    }
}
