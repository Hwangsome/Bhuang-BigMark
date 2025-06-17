package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author bhuang
 * @description 策略装配服务测试类
 * @create 2025-01-15
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StrategyAssembleTest {

    @Mock
    private IStrategyRepository strategyRepository;

    @InjectMocks
    private StrategyArmory strategyAssemble;

    @Before
    public void setUp() {
        // 设置测试基础数据
    }

    @Test
    public void testAssembleLotteryStrategy() {
        // 给定
        Long strategyId = 100001L;
        
        // 模拟策略奖品列表
        List<StrategyAwardEntity> strategyAwardList = Arrays.asList(
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(101)
                        .awardRate(new BigDecimal("0.05"))
                        .awardTitle("一等奖")
                        .awardSubtitle("iPhone 15")
                        .build(),
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(102)
                        .awardRate(new BigDecimal("0.15"))
                        .awardTitle("二等奖")
                        .awardSubtitle("iPad")
                        .build(),
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(103)
                        .awardRate(new BigDecimal("0.80"))
                        .awardTitle("三等奖")
                        .awardSubtitle("谢谢参与")
                        .build()
        );

        // 模拟权重规则（无权重）
        when(strategyRepository.queryStrategyAwardList(strategyId)).thenReturn(strategyAwardList);
        when(strategyRepository.queryStrategyRule(strategyId, "rule_weight")).thenReturn(null);

        // 当
        boolean result = strategyAssemble.assembleLotteryStrategy(strategyId);

        // 那么
        assertTrue("策略装配应该成功", result);
        verify(strategyRepository).storeStrategyAwardSearchRateTable(eq(strategyId), anyInt(), anyMap());
        log.info("测试通过 - 策略装配成功");
    }

    @Test
    public void testAssembleLotteryStrategyWithWeight() {
        // 给定
        Long strategyId = 100001L;
        
        // 模拟策略奖品列表
        List<StrategyAwardEntity> strategyAwardList = Arrays.asList(
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(101)
                        .awardRate(new BigDecimal("0.05"))
                        .awardTitle("一等奖")
                        .awardSubtitle("iPhone 15")
                        .build(),
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(102)
                        .awardRate(new BigDecimal("0.15"))
                        .awardTitle("二等奖")
                        .awardSubtitle("iPad")
                        .build(),
                StrategyAwardEntity.builder()
                        .strategyId(strategyId)
                        .awardId(103)
                        .awardRate(new BigDecimal("0.80"))
                        .awardTitle("三等奖")
                        .awardSubtitle("谢谢参与")
                        .build()
        );

        // 模拟权重规则
        StrategyRuleEntity ruleEntity = StrategyRuleEntity.builder()
                .strategyId(strategyId)
                .ruleModel("rule_weight")
                .ruleValue("4000:102,103 5000:101,102,103")
                .build();

        when(strategyRepository.queryStrategyAwardList(strategyId)).thenReturn(strategyAwardList);
        when(strategyRepository.queryStrategyRule(strategyId, "rule_weight")).thenReturn(ruleEntity);

        // 当
        boolean result = strategyAssemble.assembleLotteryStrategy(strategyId);

        // 那么
        assertTrue("带权重的策略装配应该成功", result);
        verify(strategyRepository).storeStrategyAwardSearchRateTable(eq(strategyId), anyInt(), anyMap());
        verify(strategyRepository, atLeast(1)).storeStrategyAwardSearchRateTableByWeight(eq(strategyId), anyString(), anyInt(), anyMap());
        log.info("测试通过 - 带权重的策略装配成功");
    }

    @Test
    public void testAssembleLotteryStrategyWithEmptyList() {
        // 给定
        Long strategyId = 100001L;
        when(strategyRepository.queryStrategyAwardList(strategyId)).thenReturn(Collections.emptyList());

        // 当
        boolean result = strategyAssemble.assembleLotteryStrategy(strategyId);

        // 那么
        assertFalse("空策略列表装配应该失败", result);
        verify(strategyRepository, never()).storeStrategyAwardSearchRateTable(anyLong(), anyInt(), anyMap());
        log.info("测试通过 - 空策略列表装配失败");
    }

    @Test
    public void testAssembleLotteryStrategyWithNullList() {
        // 给定
        Long strategyId = 100001L;
        when(strategyRepository.queryStrategyAwardList(strategyId)).thenReturn(null);

        // 当
        boolean result = strategyAssemble.assembleLotteryStrategy(strategyId);

        // 那么
        assertFalse("空策略列表装配应该失败", result);
        verify(strategyRepository, never()).storeStrategyAwardSearchRateTable(anyLong(), anyInt(), anyMap());
        log.info("测试通过 - null策略列表装配失败");
    }

    @Test
    public void testAssembleLotteryStrategyByActivityId() {
        // 给定
        Long activityId = 100001L;

        // 当
        Boolean result = strategyAssemble.assembleLotteryStrategyByActivityId(activityId);

        // 那么
        assertFalse("根据活动ID装配策略暂未实现，应该返回false", result);
        log.info("测试通过 - 根据活动ID装配策略暂未实现");
    }
} 