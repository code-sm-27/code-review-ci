package com.github.codesm27.codereview.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Note: bucket4j-redis requires lettuce or jedis connection factory, but here we can use 
// bucket4j-redis Jedis or Lettuce based. Let's use the standard Lettuce implementation.
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url:#{null}}")
    private String redisUrl;

    @Bean
    public RedisClient redisClient() {
        if (redisUrl != null && !redisUrl.trim().isEmpty()) {
            return RedisClient.create(redisUrl);
        }
        return RedisClient.create(RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .build());
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(io.github.bucket4j.distributed.ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(java.time.Duration.ofSeconds(10)))
                .build();
    }
}
