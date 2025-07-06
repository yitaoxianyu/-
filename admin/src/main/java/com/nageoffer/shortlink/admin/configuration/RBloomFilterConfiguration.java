package com.nageoffer.shortlink.admin.configuration;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfiguration {


    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> userRegisterCacheBloomFilter = redissonClient.getBloomFilter("xxx");
        //tryInit参数:长度,误判率(误判率越低使用函数越多)
        userRegisterCacheBloomFilter.tryInit(100000,0.001);
        return userRegisterCacheBloomFilter;
    }
}
