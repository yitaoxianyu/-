package com.nageoffer.shortlink.project.configuration;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.*;

@Configuration
public class RabbitMqConfiguration {

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

    @Bean
    public Queue statsQueue(){
        return QueueBuilder.durable(MQ_STATS_QUEUE).build();
    }
    @Bean
    public Exchange statsExchange(){
        return new DirectExchange(MQ_STATS_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue statsQueue,Exchange statsExchange){
        return BindingBuilder.bind(statsQueue).to(statsExchange).with(MQ_STATS_ROUTING_KEY).noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
        jsonMessageConverter.setCreateMessageIds(true);
        return jsonMessageConverter;
    }

}
