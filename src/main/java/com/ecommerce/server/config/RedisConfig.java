package com.ecommerce.server.config;

import com.ecommerce.server.service.ProductSearchCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                (message, pattern) -> {
                    String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

                    if (expiredKey.startsWith(ProductSearchCacheService.KEY_PREFIX)) {
                        log.info("Product search cache entry expired: {}", expiredKey);
                    }
                },
                new PatternTopic("__keyevent@*__:expired")
        );

        return container;
    }

    @Bean
    public ApplicationRunner enableRedisKeyspaceNotifications(
            RedisConnectionFactory connectionFactory
    ) {
        return args -> {
            try {
                RedisConnection connection = connectionFactory.getConnection();

                try {
                    connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
                } finally {
                    connection.close();
                }
            } catch (Exception ex) {
                log.warn(
                        "Could not enable Redis keyspace notifications; cache expiration will not be logged",
                        ex
                );
            }
        };
    }
}
