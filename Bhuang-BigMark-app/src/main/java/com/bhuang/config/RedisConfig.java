package com.bhuang.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redis 配置类
 * 基于 Redisson 实现 Redis 客户端配置
 * @author bhuang
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedisConfigProperties.class)
public class RedisConfig {

    /**
     * 创建 Redisson 客户端
     * @param redisProperties Redis 配置属性
     * @return RedissonClient
     */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisConfigProperties redisProperties) {
        Config config = new Config();
        
        // 构建 Redis 连接地址
        String address = String.format("redis://%s:%d", 
                redisProperties.getHost(), 
                redisProperties.getPort());
        
        log.info("Redis 连接地址: {}", address);
        
        // 单机模式配置
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisProperties.getDatabase())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle())
                .setConnectionPoolSize(redisProperties.getPool().getMaxActive())
                .setIdleConnectionTimeout(redisProperties.getPool().getMaxWait().intValue())
                .setConnectTimeout(redisProperties.getTimeout())
                .setTimeout(redisProperties.getTimeout())
                .setRetryAttempts(3)
                .setRetryInterval(1500);
        
        // 如果设置了密码
        if (StringUtils.hasText(redisProperties.getPassword())) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
            log.info("Redis 连接已配置密码认证");
        }
        
        // 创建支持 Java 8 时间类型的 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // 设置编解码器，支持 Java 8 时间类型
        config.setCodec(new JsonJacksonCodec(objectMapper));
        
        // 设置线程池数量
        config.setThreads(16);
        config.setNettyThreads(32);
        
        log.info("Redis 连接配置完成 - 数据库: {}, 连接池: {}, 超时: {}ms, 支持 Java 8 时间类型", 
                redisProperties.getDatabase(), 
                redisProperties.getPool().getMaxActive(),
                redisProperties.getTimeout());
        
        return Redisson.create(config);
    }
} 