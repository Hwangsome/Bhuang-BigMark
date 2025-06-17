package com.bhuang.domain.strategy.service.armory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author bhuang
 * @description 策略调度服务 - 专门负责抽奖逻辑的执行
 * @create 2025-06-13
 */
@Slf4j
@Service
public class StrategyDispatchService implements IStrategyDispatch {

    @Resource
    private StrategyArmory strategyArmory;

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        return strategyArmory.getRandomAwardId(strategyId);
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        return strategyArmory.getRandomAwardId(strategyId, ruleWeightValue);
    }
} 