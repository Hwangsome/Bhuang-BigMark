package com.bhuang.domain.strategy.service.armory;

/**
 * 策略装配接口
 * 负责策略的初始化和装配操作，通常在活动创建或审核通过后调用
 * 
 * @author bhuang
 */
public interface IStrategyAssemble {

    /**
     * 装配抽奖策略配置
     * 触发时机：活动审核通过后进行调用
     * 
     * @param strategyId 策略ID
     * @return 装配结果 true-成功 false-失败
     */
    boolean assembleLotteryStrategy(Long strategyId);

    /**
     * 根据活动ID装配抽奖策略
     * 触发时机：活动创建或配置更新时调用
     * 
     * @param activityId 活动ID
     * @return 装配结果 true-成功 false-失败
     */
    Boolean assembleLotteryStrategyByActivityId(Long activityId);
} 