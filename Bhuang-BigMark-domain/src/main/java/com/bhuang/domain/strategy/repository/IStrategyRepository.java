package com.bhuang.domain.strategy.repository;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;

import java.util.List;
import java.util.Map;

/**
 * @author bhuang
 * @description 策略服务仓储接口
 * @create 2025-06-13
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTable(Long strategyId, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable);

    Integer getStrategyAwardAssemble(Long strategyId, Integer rateKey);

    int getRateRange(Long strategyId);

    /**
     * 查询策略规则
     * @param strategyId 策略ID
     * @param ruleModel 规则模型
     * @return 策略规则实体
     */
    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);

    /**
     * 存储策略权重概率查找表
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @param rateRange 概率范围
     * @param strategyAwardSearchRateTable 权重概率查找表
     */
    void storeStrategyAwardSearchRateTableByWeight(Long strategyId, String ruleWeightValue, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable);

    /**
     * 获取权重概率范围
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @return 概率范围
     */
    int getRateRangeByWeight(Long strategyId, String ruleWeightValue);

    /**
     * 获取权重策略奖品装配
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @param rateKey 概率key
     * @return 奖品ID
     */
    Integer getStrategyAwardAssembleByWeight(Long strategyId, String ruleWeightValue, Integer rateKey);

}
