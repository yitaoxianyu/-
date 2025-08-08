package com.nageoffer.shortlink.project.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotentUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "short-link:idempotent:";

    public boolean isProcessed(String messageId){
        return !Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + messageId, "",2, TimeUnit.MINUTES));
    }

    public boolean del(String messageId){
        return stringRedisTemplate.delete(KEY_PREFIX + messageId);
    }


}
