package com.bhuang.test;

import com.bhuang.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Redisson自动配置测试
 * @author bhuang
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
public class RedissonAutoConfigTest {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Test
    public void testRedissonAutoConfiguration() {
        log.info("=== 测试Redisson自动配置 ===");
        
        if (redissonClient != null) {
            log.info("Redisson自动配置成功！客户端类型: {}", redissonClient.getClass().getSimpleName());
            // 测试基本连接
            try {
                boolean isShutdown = redissonClient.isShutdown();
                log.info("Redisson客户端状态 - isShutdown: {}", isShutdown);
                assertTrue("Redisson客户端应该处于运行状态", !isShutdown);
            } catch (Exception e) {
                log.error("测试Redisson连接失败: {}", e.getMessage());
                fail("Redisson连接测试失败: " + e.getMessage());
            }
        } else {
            log.error("Redisson未被自动配置！");
            fail("Redisson应该被自动配置");
        }
    }
} 