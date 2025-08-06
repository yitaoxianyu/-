package com.nageoffer.shortlink.project.mq;


import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.DELAY_QUEUE_STATS_KEY;

/*
    构建初始化 bean 的操作顺序:
    属性注入 -> 执行 afterPropertiesSet(如果有) -> @PostConstruct方法 -> postProcess
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final ShortLinkService shortLinkService;

    private final RedissonClient redissonClient;

    private void onMessage(){
        Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("delay_short-link_stats_consumer");
            return thread;
        }).execute(() -> {
            RBlockingDeque<ShortLinkStatsRecordDTO> delayQueue = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
            for(;;){
                try {
                    ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = delayQueue.poll();
                    if (shortLinkStatsRecordDTO != null) {
                        shortLinkService.recordStats(null, null, shortLinkStatsRecordDTO);
                        continue;
                    }
                    LockSupport.parkUntil(500);
                } catch (Throwable e) {
                }
            }
        });
    }

    @Override
    public void afterPropertiesSet() {
        onMessage();
    }
}
