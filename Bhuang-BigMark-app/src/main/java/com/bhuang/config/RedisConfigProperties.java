package com.bhuang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 配置属性
 * @author bhuang
 */
@Data
@ConfigurationProperties(prefix = "spring.redis", ignoreInvalidFields = true)
public class RedisConfigProperties {

    /** Redis 服务器地址 */
    private String host = "127.0.0.1";
    
    /** Redis 服务器端口 */
    private Integer port = 6379;
    
    /** Redis 服务器密码 */
    private String password;
    
    /** 数据库索引（默认为0） */
    private Integer database = 0;
    
    /** 连接超时时间（毫秒） */
    private Integer timeout = 3000;
    
    /** Jedis 连接池配置 */
    private Pool pool = new Pool();
    
    @Data
    public static class Pool {
        /** 连接池最大连接数 */
        private Integer maxActive = 50;
        
        /** 连接池最大空闲连接数 */
        private Integer maxIdle = 10;
        
        /** 连接池最小空闲连接数 */
        private Integer minIdle = 5;
        
        /** 连接池最大阻塞等待时间（毫秒） */
        private Long maxWait = 3000L;
        
        /** 是否在从池中取出连接前进行检验，如果检验失败，则从池中去除连接并尝试取出另一个 */
        private Boolean testOnBorrow = true;
        
        /** 是否在归还到池中前进行检验 */
        private Boolean testOnReturn = false;
        
        /** 是否在空闲时进行连接检验 */
        private Boolean testWhileIdle = true;
    }
} 