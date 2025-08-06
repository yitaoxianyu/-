package com.nageoffer.shortlink.project.configuration;

import com.nageoffer.shortlink.project.mq.ShortLinkStatsConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ShortLinkStatsMqConfiguration {

    private final ShortLinkStatsConsumer shortLinkStatsConsumer;
    private final RedisConnectionFactory redisConnectionFactory;

    private static final String topic = "short-link:stream:stats";
    private static final String group = "short-link:stats-stream:only-group";

    @Bean
    public ExecutorService asyncExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(
                processors,
                processors + processors >> 1,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("stream_consumer_short-link_stats_" + index.incrementAndGet());
                    return thread;
                }
                );
    }

    @Bean(initMethod = "start",destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(ExecutorService asyncExecutor){
        // 1. 创建Stream和消费者组（如果不存在）
        try {
            RedisConnection conn = redisConnectionFactory.getConnection();
            try {
                conn.xGroupCreate(topic.getBytes(), group, ReadOffset.from("0-0"), true);
            } catch (RedisSystemException e) {
                if (!e.getCause().getMessage().contains("BUSYGROUP")) {
                    throw e;
                }
                log.info("消费者组已存在: {}", group);
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            log.warn("初始化Stream/Group失败", e);
        }

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次最多获取多少条消息
                        .batchSize(10)
                        // 执行从 Stream 拉取到消息的任务流程
                        .executor(asyncExecutor)
                        // 如果没有拉取到消息，需要阻塞的时间。不能大于 ${spring.data.redis.timeout}，否则会超时
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);
        streamMessageListenerContainer.receiveAutoAck(
                Consumer.from(group,"c1"),
                StreamOffset.create(topic, ReadOffset.lastConsumed()),
                shortLinkStatsConsumer);
        return streamMessageListenerContainer;
    }
}
