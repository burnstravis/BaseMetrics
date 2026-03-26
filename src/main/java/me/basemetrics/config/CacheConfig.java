package me.basemetrics.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {


    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
                buildCache("players", 1, TimeUnit.HOURS, 500),
                buildCache("teams", 24, TimeUnit.HOURS, 32),
                buildCache("liveGames", 15, TimeUnit.SECONDS, 100)
        ));

        return cacheManager;
    }

    private CaffeineCache buildCache(String name, int ttl, TimeUnit unit, int size) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl, unit)
                .maximumSize(size)
                .recordStats()
                .build());
    }
}
