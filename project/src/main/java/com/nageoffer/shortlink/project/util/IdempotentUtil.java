package com.nageoffer.shortlink.project.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotentUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "short-link:idempotent:";


    public boolean isProcessed(String messageId){
        return !Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + messageId, "0",2, TimeUnit.MINUTES));
    }
    //1代表完成状态
    public boolean isAccomplish(String messageId){
         return Objects.equals(stringRedisTemplate.opsForValue().get(KEY_PREFIX + messageId), "1");
    }

    public void setAccomplish(String messageId){
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + messageId,"1");
    }

    public void del(String messageId){
        stringRedisTemplate.delete(KEY_PREFIX + messageId);
    }


}
