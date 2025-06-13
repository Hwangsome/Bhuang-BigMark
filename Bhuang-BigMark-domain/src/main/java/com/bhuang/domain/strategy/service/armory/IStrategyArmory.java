package com.bhuang.domain.strategy.service.armory;

/**
 * 策略装配库接口
 * @author bhuang
 */
public interface IStrategyArmory {

    /**
     * 装配抽奖策略配置「触发的时机可以为活动审核通过后进行调用」
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    boolean assembleLotteryStrategy(Long strategyId);

    /**
     * 获取抽奖策略装配的随机结果
     *
     * @param strategyId 策略ID
     * @return 抽奖结果
     */
    Integer getRandomAwardId(Long strategyId);

    /**
     * 获取抽奖策略装配的随机结果 - 带权重规则
     *
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @return 抽奖结果
     */
    Integer getRandomAwardId(Long strategyId, String ruleWeightValue);

    /**
     * 根据活动ID装配抽奖策略
     *
     * @param activityId 活动ID
     * @return 装配结果
     */
    Boolean assembleLotteryStrategyByActivityId(Long activityId);

}
