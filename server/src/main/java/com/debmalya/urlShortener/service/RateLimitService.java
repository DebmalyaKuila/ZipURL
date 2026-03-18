package com.debmalya.urlShortener.service;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RateLimitService {
    private static final int REQUESTS_PER_MINUTE = 2;

    //storage for tracking request counts per user/IP
    private final ProxyManager<String> proxyManager;

    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configSupplier = () -> getConfig();
        return proxyManager.builder().build(key, configSupplier);
    }

    //Act as a factory for creating BucketConfiguration objects for newly created buckets
    public BucketConfiguration getConfig() {
        var limit = Bandwidth.builder()
                  .capacity(REQUESTS_PER_MINUTE)
                  .refillIntervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                  .build();
        return BucketConfiguration.builder()
                                  .addLimit(limit)
                                  .build();
    }
}
