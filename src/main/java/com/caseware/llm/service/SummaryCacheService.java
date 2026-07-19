package com.caseware.llm.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class SummaryCacheService {

    private static final Duration SUMMARY_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final Map<String, String> localCache = new ConcurrentHashMap<>();

    public SummaryCacheService(StringRedisTemplate redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public Optional<String> getSummary(String key) {
        if (redisTemplate != null) {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                localCache.put(key, value);
                return Optional.of(value);
            }
        }

        return Optional.ofNullable(localCache.get(key));
    }

    @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public void saveSummary(String key, String value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, SUMMARY_TTL);
        }
        localCache.put(key, value);
    }

    @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        if (redissonClient == null) {
            return action.get();
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Unable to acquire summary generation lock");
            }
            return action.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for summary generation lock", ex);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }
}
