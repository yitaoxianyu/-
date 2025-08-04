package com.nageoffer.shortlink.project.mq;

import com.nageoffer.shortlink.project.common.constants.ShortLinkConstants;
import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    public void add(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO){
        RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(ShortLinkConstants.DELAY_QUEUE_STATS_KEY);
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        //这里延迟 5s,等待更新完之后再投递
        delayedQueue.offer(shortLinkStatsRecordDTO,5, TimeUnit.SECONDS);
    }

}
