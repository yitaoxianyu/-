package com.nageoffer.shortlink.project.configuration;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfiguration {

    @Bean
    public RBloomFilter<String> shortUriCreateBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> shortUriCreateBloomFilter = redissonClient.getBloomFilter("shortlink");
        shortUriCreateBloomFilter.tryInit(100000000,0.001);
        return shortUriCreateBloomFilter;
    }
}
