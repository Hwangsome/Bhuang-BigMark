package com.bhuang.domain.strategy.service.armory;

/**
 * 策略调度接口
 * 负责执行抽奖逻辑，从已装配的策略中获取随机奖品
 * 
 * @author bhuang
 */
public interface IStrategyDispatch {

    /**
     * 获取抽奖策略装配的随机结果
     * 
     * @param strategyId 策略ID
     * @return 抽奖结果 奖品ID
     */
    Integer getRandomAwardId(Long strategyId);

    /**
     * 获取抽奖策略装配的随机结果 - 带权重规则
     * 
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值（如：4000、5000、6000）
     * @return 抽奖结果 奖品ID
     */
    Integer getRandomAwardId(Long strategyId, String ruleWeightValue);
} 