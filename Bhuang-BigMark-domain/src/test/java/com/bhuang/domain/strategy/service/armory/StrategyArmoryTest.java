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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author bhuang
 * @description 策略装配器测试
 * @create 2025-06-13
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StrategyArmoryTest {

    @Mock
    private IStrategyRepository strategyRepository;

    @InjectMocks
    private StrategyArmory strategyArmory;

    private List<StrategyAwardEntity> mockStrategyAwardList;
    private StrategyRuleEntity mockStrategyRuleEntity;

    @Before
    public void setUp() {
        // 模拟策略奖品列表
        mockStrategyAwardList = Arrays.asList(
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(101)
                .awardTitle("一等奖")
                .awardSubtitle("奖金1000元")
                .awardCount(1)
                .awardCountSurplus(1)
                .awardRate(new BigDecimal("0.1"))
                .sort(1)
                .build(),
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(102)
                .awardTitle("二等奖")
                .awardSubtitle("奖金500元")
                .awardCount(10)
                .awardCountSurplus(10)
                .awardRate(new BigDecimal("1"))
                .sort(2)
                .build(),
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(103)
                .awardTitle("三等奖")
                .awardSubtitle("奖金100元")
                .awardCount(50)
                .awardCountSurplus(50)
                .awardRate(new BigDecimal("5"))
                .sort(3)
                .build(),
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(104)
                .awardTitle("四等奖")
                .awardSubtitle("奖金50元")
                .awardCount(100)
                .awardCountSurplus(100)
                .awardRate(new BigDecimal("10"))
                .sort(4)
                .build(),
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(105)
                .awardTitle("五等奖")
                .awardSubtitle("小礼品")
                .awardCount(500)
                .awardCountSurplus(500)
                .awardRate(new BigDecimal("20"))
                .sort(5)
                .build(),
            StrategyAwardEntity.builder()
                .strategyId(100001L)
                .awardId(106)
                .awardTitle("六等奖")
                .awardSubtitle("谢谢参与")
                .awardCount(1000)
                .awardCountSurplus(1000)
                .awardRate(new BigDecimal("63.9"))
                .sort(6)
                .build()
        );

        // 模拟权重规则
        mockStrategyRuleEntity = StrategyRuleEntity.builder()
            .strategyId(100001L)
            .ruleModel("rule_weight")
            .ruleValue("4000:102,103,104,105 5000:102,103,104,105,106 6000:102,103,104,105,106,107")
            .ruleDesc("权重规则配置")
            .build();
    }

    @Test
    public void testAssembleLotteryStrategy() {
        log.info("开始测试策略装配功能");

        // 模拟数据查询
        when(strategyRepository.queryStrategyAwardList(100001L)).thenReturn(mockStrategyAwardList);
        when(strategyRepository.queryStrategyRule(100001L, "rule_weight")).thenReturn(mockStrategyRuleEntity);

        // 执行装配
        boolean result = strategyArmory.assembleLotteryStrategy(100001L);

        // 验证结果
        assertTrue("策略装配应该成功", result);

        // 验证方法调用
        verify(strategyRepository, times(1)).queryStrategyAwardList(100001L);
        verify(strategyRepository, times(1)).queryStrategyRule(100001L, "rule_weight");
        
        // 验证正常策略存储
        verify(strategyRepository, times(1)).storeStrategyAwardSearchRateTable(
            eq(100001L), 
            anyInt(), 
            any(Map.class)
        );
        
        // 验证权重策略存储（应该调用3次，分别对应3个权重配置）
        verify(strategyRepository, times(3)).storeStrategyAwardSearchRateTableByWeight(
            eq(100001L), 
            anyString(), 
            anyInt(), 
            any(Map.class)
        );

        log.info("策略装配测试完成");
    }

    @Test
    public void testGetRandomAwardIdNormal() {
        log.info("开始测试普通抽奖功能");

        // 模拟数据
        when(strategyRepository.getRateRange(100001L)).thenReturn(10000);
        when(strategyRepository.getStrategyAwardAssemble(eq(100001L), anyInt())).thenReturn(106);

        // 执行抽奖
        Integer awardId = strategyArmory.getRandomAwardId(100001L);

        // 验证结果
        assertNotNull("奖品ID不应该为空", awardId);
        assertEquals("奖品ID应该为106", Integer.valueOf(106), awardId);

        // 验证方法调用
        verify(strategyRepository, times(1)).getRateRange(100001L);
        verify(strategyRepository, times(1)).getStrategyAwardAssemble(eq(100001L), anyInt());

        log.info("普通抽奖测试完成，奖品ID：{}", awardId);
    }

    @Test
    public void testGetRandomAwardIdWithWeight() {
        log.info("开始测试权重抽奖功能");

        // 模拟数据
        when(strategyRepository.getRateRangeByWeight(100001L, "6000")).thenReturn(5000);
        when(strategyRepository.getStrategyAwardAssembleByWeight(eq(100001L), eq("6000"), anyInt())).thenReturn(102);

        // 执行权重抽奖
        Integer awardId = strategyArmory.getRandomAwardId(100001L, "6000");

        // 验证结果
        assertNotNull("奖品ID不应该为空", awardId);
        assertEquals("奖品ID应该为102", Integer.valueOf(102), awardId);

        // 验证方法调用
        verify(strategyRepository, times(1)).getRateRangeByWeight(100001L, "6000");
        verify(strategyRepository, times(1)).getStrategyAwardAssembleByWeight(eq(100001L), eq("6000"), anyInt());

        log.info("权重抽奖测试完成，奖品ID：{}", awardId);
    }

    @Test
    public void testGetRandomAwardIdWithWeightFallback() {
        log.info("开始测试权重抽奖降级功能");

        // 模拟权重策略不存在，使用普通策略降级
        when(strategyRepository.getRateRangeByWeight(100001L, "9000")).thenReturn(0);
        when(strategyRepository.getRateRange(100001L)).thenReturn(10000);
        when(strategyRepository.getStrategyAwardAssemble(eq(100001L), anyInt())).thenReturn(106);

        // 执行权重抽奖（应该降级到普通抽奖）
        Integer awardId = strategyArmory.getRandomAwardId(100001L, "9000");

        // 验证结果
        assertNotNull("奖品ID不应该为空", awardId);
        assertEquals("奖品ID应该为106", Integer.valueOf(106), awardId);

        // 验证方法调用
        verify(strategyRepository, times(1)).getRateRangeByWeight(100001L, "9000");
        verify(strategyRepository, times(1)).getRateRange(100001L);
        verify(strategyRepository, times(1)).getStrategyAwardAssemble(eq(100001L), anyInt());

        log.info("权重抽奖降级测试完成，奖品ID：{}", awardId);
    }

    @Test
    public void testAssembleLotteryStrategyWithoutWeightRule() {
        log.info("开始测试无权重规则的策略装配");

        // 模拟无权重规则
        when(strategyRepository.queryStrategyAwardList(100001L)).thenReturn(mockStrategyAwardList);
        when(strategyRepository.queryStrategyRule(100001L, "rule_weight")).thenReturn(null);

        // 执行装配
        boolean result = strategyArmory.assembleLotteryStrategy(100001L);

        // 验证结果
        assertTrue("策略装配应该成功", result);

        // 验证方法调用
        verify(strategyRepository, times(1)).queryStrategyAwardList(100001L);
        verify(strategyRepository, times(1)).queryStrategyRule(100001L, "rule_weight");
        
        // 验证只调用了正常策略存储，没有调用权重策略存储
        verify(strategyRepository, times(1)).storeStrategyAwardSearchRateTable(
            eq(100001L), 
            anyInt(), 
            any(Map.class)
        );
        verify(strategyRepository, never()).storeStrategyAwardSearchRateTableByWeight(
            anyLong(), 
            anyString(), 
            anyInt(), 
            any(Map.class)
        );

        log.info("无权重规则策略装配测试完成");
    }

    @Test
    public void testAssembleLotteryStrategyWithEmptyAwardList() {
        log.info("开始测试空奖品列表的策略装配");

        // 模拟空奖品列表
        when(strategyRepository.queryStrategyAwardList(100001L)).thenReturn(Arrays.asList());

        // 执行装配
        boolean result = strategyArmory.assembleLotteryStrategy(100001L);

        // 验证结果
        assertFalse("策略装配应该失败", result);

        // 验证方法调用
        verify(strategyRepository, times(1)).queryStrategyAwardList(100001L);
        verify(strategyRepository, never()).queryStrategyRule(anyLong(), anyString());

        log.info("空奖品列表策略装配测试完成");
    }

    @Test
    public void testStrategyRuleEntityWeightParsing() {
        log.info("开始测试策略规则实体权重解析功能");

        // 测试权重规则解析
        Map<String, String[]> weightValues = mockStrategyRuleEntity.getRuleWeightValues();
        
        // 验证解析结果
        assertNotNull("权重配置不应该为空", weightValues);
        assertEquals("应该有3个权重配置", 3, weightValues.size());
        
        assertTrue("应该包含4000权重", weightValues.containsKey("4000"));
        assertTrue("应该包含5000权重", weightValues.containsKey("5000"));
        assertTrue("应该包含6000权重", weightValues.containsKey("6000"));
        
        // 验证4000权重的奖品ID
        String[] awardIds4000 = weightValues.get("4000");
        assertEquals("4000权重应该有4个奖品", 4, awardIds4000.length);
        assertArrayEquals("4000权重奖品ID应该匹配", new String[]{"102", "103", "104", "105"}, awardIds4000);
        
        // 验证5000权重的奖品ID
        String[] awardIds5000 = weightValues.get("5000");
        assertEquals("5000权重应该有5个奖品", 5, awardIds5000.length);
        assertArrayEquals("5000权重奖品ID应该匹配", new String[]{"102", "103", "104", "105", "106"}, awardIds5000);
        
        // 验证6000权重的奖品ID
        String[] awardIds6000 = weightValues.get("6000");
        assertEquals("6000权重应该有6个奖品", 6, awardIds6000.length);
        assertArrayEquals("6000权重奖品ID应该匹配", new String[]{"102", "103", "104", "105", "106", "107"}, awardIds6000);

        log.info("策略规则实体权重解析测试完成");
    }

    @Test
    public void testGetRandomAwardIdWithNullStrategy() {
        log.info("开始测试不存在策略的抽奖");

        // 模拟策略不存在
        when(strategyRepository.getRateRange(999999L)).thenReturn(0);

        // 执行抽奖
        Integer awardId = strategyArmory.getRandomAwardId(999999L);

        // 验证结果
        assertNull("不存在的策略应该返回null", awardId);

        log.info("不存在策略抽奖测试完成");
    }
} 