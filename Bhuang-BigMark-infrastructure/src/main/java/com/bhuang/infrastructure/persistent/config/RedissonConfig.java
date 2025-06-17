package com.bhuang.infrastructure.persistent.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Redisson配置类
 * 解决Redis中value乱码问题，使用JSON序列化
 * @author bhuang
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.redisson.config:#{null}}")
    private String redissonConfig;

    @Value("${spring.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.redis.port:26379}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.password:#{null}}")
    private String password;

    @Value("${spring.redis.timeout:3000}")
    private int timeout;

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 使用JSON序列化，解决乱码问题
        config.setCodec(new JsonJacksonCodec());
        
        // 配置单机Redis服务器
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(2)
                .setTimeout(timeout)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
        
        // 如果有密码，设置密码
        if (password != null && !password.trim().isEmpty()) {
            config.useSingleServer().setPassword(password);
        }
        
        return Redisson.create(config);
    }
} 