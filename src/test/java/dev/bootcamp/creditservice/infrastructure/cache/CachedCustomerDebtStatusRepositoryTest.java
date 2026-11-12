package dev.bootcamp.creditservice.infrastructure.cache;

import dev.bootcamp.creditservice.domain.model.debt.CustomerDebtStatus;
import dev.bootcamp.creditservice.infrastructure.persistence.CustomerDebtStatusDocument;
import dev.bootcamp.creditservice.infrastructure.persistence.CustomerDebtStatusMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedCustomerDebtStatusRepositoryTest {

    private static final Duration TTL = Duration.ofMinutes(10);

    @Mock private CustomerDebtStatusMongoRepository mongoRepository;

    @Mock private ReactiveStringRedisTemplate redisTemplate;

    @Mock private ReactiveValueOperations<String, String> valueOperations;

    private CachedCustomerDebtStatusRepository repository;

    @BeforeEach
    void createRepository() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        repository = new CachedCustomerDebtStatusRepository(mongoRepository, redisTemplate, TTL);
    }

    @Test
    void savesInMongoAndCachesTheFlag() {
        when(mongoRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.<CustomerDebtStatusDocument>getArgument(0)));
        when(valueOperations.set("debt-status:c1", "true", TTL)).thenReturn(Mono.just(true));

        repository.save(new CustomerDebtStatus(
                        "c1",
                        true,
                        BigDecimal.TEN,
                        Instant.parse("2026-11-15T10:00:00Z")))
                .test()
                .assertComplete();

        verify(valueOperations).set("debt-status:c1", "true", TTL);
    }

    @Test
    void readsFlagFromCacheWithoutQueryingMongo() {
        when(valueOperations.get("debt-status:c1")).thenReturn(Mono.just("true"));
        when(mongoRepository.findById("c1")).thenReturn(Mono.empty());

        repository.hasOverdueDebt("c1").test().assertValue(true);

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void readsFromMongoOnCacheMissAndCachesIt() {
        when(valueOperations.get("debt-status:c1")).thenReturn(Mono.empty());
        when(mongoRepository.findById("c1")).thenReturn(Mono.just(new CustomerDebtStatusDocument(
                "c1",
                true,
                BigDecimal.TEN,
                Instant.parse("2026-11-15T10:00:00Z"))));
        when(valueOperations.set("debt-status:c1", "true", TTL)).thenReturn(Mono.just(true));

        repository.hasOverdueDebt("c1").test().assertValue(true);
    }

    @Test
    void unknownCustomerHasNoOverdueDebtEvenIfRedisFails() {
        when(valueOperations.get("debt-status:c9"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));
        when(mongoRepository.findById("c9")).thenReturn(Mono.empty());
        when(valueOperations.set("debt-status:c9", "false", TTL))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        repository.hasOverdueDebt("c9").test().assertValue(false);
    }
}
