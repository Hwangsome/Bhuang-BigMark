package com.bhuang.domain.strategy.service.armory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author bhuang
 * @description 策略装配服务 - 专门负责策略的初始化和装配
 * @create 2025-06-13
 */
@Slf4j
@Service
public class StrategyAssembleService implements IStrategyAssemble {

    @Resource
    private StrategyArmory strategyArmory;

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        log.info("开始装配抽奖策略，策略ID：{}", strategyId);
        return strategyArmory.assembleLotteryStrategy(strategyId);
    }

    @Override
    public Boolean assembleLotteryStrategyByActivityId(Long activityId) {
        log.info("根据活动ID装配抽奖策略，活动ID：{}", activityId);
        return strategyArmory.assembleLotteryStrategyByActivityId(activityId);
    }
} 