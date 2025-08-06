package com.nageoffer.shortlink.project.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShortLinkStatsProducer {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String topic = "short-link:stream:stats";

    public void send(Map<String,String> map){
        stringRedisTemplate.opsForStream().add(MapRecord.create(
                topic,map
        ));
    }
}
