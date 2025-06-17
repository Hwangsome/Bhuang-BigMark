package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author bhuang
 * @description 策略调度服务测试类
 * @create 2025-01-15
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StrategyDispatchTest {

    @Mock
    private IStrategyRepository strategyRepository;

    @InjectMocks
    private StrategyArmory strategyDispatch;

    @Before
    public void setUp() {
        // 设置测试基础数据
    }

    @Test
    public void testGetRandomAwardId() {
        // 给定
        Long strategyId = 100001L;
        int rateRange = 10000;
        Integer expectedAwardId = 101;

        when(strategyRepository.getRateRange(strategyId)).thenReturn(rateRange);
        when(strategyRepository.getStrategyAwardAssemble(eq(strategyId), anyInt())).thenReturn(expectedAwardId);

        // 当
        Integer result = strategyDispatch.getRandomAwardId(strategyId);

        // 那么
        assertNotNull("应该返回奖品ID", result);
        assertEquals("应该返回预期的奖品ID", expectedAwardId, result);
        verify(strategyRepository).getRateRange(strategyId);
        verify(strategyRepository).getStrategyAwardAssemble(eq(strategyId), anyInt());
        log.info("测试通过 - 随机获取奖品ID成功，奖品ID：{}", result);
    }

    @Test
    public void testGetRandomAwardIdWithWeightRule() {
        // 给定
        Long strategyId = 100001L;
        String ruleWeightValue = "4000:102,103";
        int rateRange = 5000;
        Integer expectedAwardId = 102;

        when(strategyRepository.getRateRangeByWeight(strategyId, ruleWeightValue)).thenReturn(rateRange);
        when(strategyRepository.getStrategyAwardAssembleByWeight(eq(strategyId), eq(ruleWeightValue), anyInt())).thenReturn(expectedAwardId);

        // 当
        Integer result = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValue);

        // 那么
        assertNotNull("应该返回奖品ID", result);
        assertEquals("应该返回预期的奖品ID", expectedAwardId, result);
        verify(strategyRepository).getRateRangeByWeight(strategyId, ruleWeightValue);
        verify(strategyRepository).getStrategyAwardAssembleByWeight(eq(strategyId), eq(ruleWeightValue), anyInt());
        log.info("测试通过 - 权重随机获取奖品ID成功，奖品ID：{}", result);
    }

    @Test
    public void testGetRandomAwardIdWithUnassembledStrategy() {
        // 给定
        Long strategyId = 100001L;

        when(strategyRepository.getRateRange(strategyId)).thenReturn(0);

        // 当
        Integer result = strategyDispatch.getRandomAwardId(strategyId);

        // 那么
        assertNull("未装配的策略应该返回null", result);
        verify(strategyRepository).getRateRange(strategyId);
        verify(strategyRepository, never()).getStrategyAwardAssemble(anyLong(), anyInt());
        log.info("测试通过 - 未装配策略返回null");
    }

    @Test
    public void testGetRandomAwardIdWithWeightRuleFallback() {
        // 给定
        Long strategyId = 100001L;
        String ruleWeightValue = "4000:102,103";
        int normalRateRange = 10000;
        Integer expectedAwardId = 103;

        // 权重策略未装配，回退到普通策略
        when(strategyRepository.getRateRangeByWeight(strategyId, ruleWeightValue)).thenReturn(0);
        when(strategyRepository.getRateRange(strategyId)).thenReturn(normalRateRange);
        when(strategyRepository.getStrategyAwardAssemble(eq(strategyId), anyInt())).thenReturn(expectedAwardId);

        // 当
        Integer result = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValue);

        // 那么
        assertNotNull("应该回退到普通策略并返回奖品ID", result);
        assertEquals("应该返回预期的奖品ID", expectedAwardId, result);
        verify(strategyRepository).getRateRangeByWeight(strategyId, ruleWeightValue);
        verify(strategyRepository).getRateRange(strategyId);
        verify(strategyRepository).getStrategyAwardAssemble(eq(strategyId), anyInt());
        verify(strategyRepository, never()).getStrategyAwardAssembleByWeight(anyLong(), anyString(), anyInt());
        log.info("测试通过 - 权重策略回退到普通策略成功，奖品ID：{}", result);
    }

    @Test
    public void testGetRandomAwardIdWithNullReturn() {
        // 给定
        Long strategyId = 100001L;
        int rateRange = 10000;

        when(strategyRepository.getRateRange(strategyId)).thenReturn(rateRange);
        when(strategyRepository.getStrategyAwardAssemble(eq(strategyId), anyInt())).thenReturn(null);

        // 当
        Integer result = strategyDispatch.getRandomAwardId(strategyId);

        // 那么
        assertNull("策略查找表可能有问题，应该返回null", result);
        verify(strategyRepository).getRateRange(strategyId);
        verify(strategyRepository).getStrategyAwardAssemble(eq(strategyId), anyInt());
        log.info("测试通过 - 策略查找表异常返回null");
    }

    @Test
    public void testMultipleRandomCalls() {
        // 给定
        Long strategyId = 100001L;
        int rateRange = 10000;

        when(strategyRepository.getRateRange(strategyId)).thenReturn(rateRange);
        when(strategyRepository.getStrategyAwardAssemble(eq(strategyId), anyInt()))
                .thenReturn(101, 102, 103, 101, 102); // 模拟多次返回不同奖品

        // 当 - 执行多次抽奖
        for (int i = 0; i < 5; i++) {
            Integer result = strategyDispatch.getRandomAwardId(strategyId);
            assertNotNull("每次抽奖都应该返回奖品ID", result);
            log.info("第{}次抽奖结果：{}", i + 1, result);
        }

        // 那么
        verify(strategyRepository, times(5)).getRateRange(strategyId);
        verify(strategyRepository, times(5)).getStrategyAwardAssemble(eq(strategyId), anyInt());
        log.info("测试通过 - 多次随机抽奖成功");
    }
} 