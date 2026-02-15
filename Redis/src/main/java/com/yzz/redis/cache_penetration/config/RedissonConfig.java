package com.yzz.redis.cache_penetration.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RBloomFilter<String> productBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("product:register:bloom");
        bloomFilter.tryInit(10000000, 0.01);
        return bloomFilter;
    }
}
