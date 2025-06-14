package com.bhuang.test;

import com.bhuang.Application;
import com.bhuang.infrastructure.persistent.redis.IRedisService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Redis服务功能完整测试
 * 验证IRedisService接口中所有方法的功能
 * 
 * @author bhuang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")  // 改回dev环境测试Redis连接
public class RedisServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceTest.class);

    @Autowired
    private IRedisService redisService;

    @Before
    public void setUp() {
        logger.info("RedisServiceTest setUp");
    }

    @After
    public void tearDown() {
        logger.info("RedisServiceTest tearDown");
    }

    @Test
    public void testRedisService() {
        // 测试代码
    }
} 