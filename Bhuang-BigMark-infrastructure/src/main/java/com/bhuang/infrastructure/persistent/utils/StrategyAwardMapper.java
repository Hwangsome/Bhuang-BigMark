package com.bhuang.infrastructure.persistent.utils;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.infrastructure.persistent.po.StrategyAward;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * StrategyAward 和 StrategyAwardEntity 互转工具类
 * @author bhuang
 */
public class StrategyAwardMapper {

    /**
     * 将 StrategyAward PO 转换为 StrategyAwardEntity 领域实体
     * @param strategyAward PO对象
     * @return 领域实体
     */
    public static StrategyAwardEntity toEntity(StrategyAward strategyAward) {
        if (strategyAward == null) {
            return null;
        }
        
        return new StrategyAwardEntity(
            strategyAward.getStrategyId(),
            strategyAward.getAwardId(),
            strategyAward.getAwardTitle(),
            strategyAward.getAwardSubtitle(),
            strategyAward.getAwardCount(),
            strategyAward.getAwardCountSurplus(),
            strategyAward.getAwardRate(),
            strategyAward.getRuleModels(),
            strategyAward.getSort()
        );
    }

    /**
     * 将 StrategyAwardEntity 领域实体转换为 StrategyAward PO
     * @param entity 领域实体
     * @return PO对象
     */
    public static StrategyAward toPO(StrategyAwardEntity entity) {
        if (entity == null) {
            return null;
        }
        
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(entity.getStrategyId());
        strategyAward.setAwardId(entity.getAwardId());
        strategyAward.setAwardTitle(entity.getAwardTitle());
        strategyAward.setAwardSubtitle(entity.getAwardSubtitle());
        strategyAward.setAwardCount(entity.getAwardCount());
        strategyAward.setAwardCountSurplus(entity.getAwardCountSurplus());
        strategyAward.setAwardRate(entity.getAwardRate());
        strategyAward.setRuleModels(entity.getRuleModels());
        strategyAward.setSort(entity.getSort());
        
        return strategyAward;
    }

    /**
     * 将 StrategyAward PO 列表转换为 StrategyAwardEntity 领域实体列表
     * @param strategyAwardList PO对象列表
     * @return 领域实体列表
     */
    public static List<StrategyAwardEntity> toEntityList(List<StrategyAward> strategyAwardList) {
        if (strategyAwardList == null || strategyAwardList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return strategyAwardList.stream()
                .map(StrategyAwardMapper::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将 StrategyAwardEntity 领域实体列表转换为 StrategyAward PO 列表
     * @param entityList 领域实体列表
     * @return PO对象列表
     */
    public static List<StrategyAward> toPOList(List<StrategyAwardEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return entityList.stream()
                .map(StrategyAwardMapper::toPO)
                .collect(Collectors.toList());
    }

    /**
     * 将 StrategyAwardEntity 的数据填充到已存在的 StrategyAward 对象中
     * @param entity 领域实体
     * @param strategyAward 目标PO对象
     */
    public static void fillPO(StrategyAwardEntity entity, StrategyAward strategyAward) {
        if (entity == null || strategyAward == null) {
            return;
        }
        
        strategyAward.setStrategyId(entity.getStrategyId());
        strategyAward.setAwardId(entity.getAwardId());
        strategyAward.setAwardTitle(entity.getAwardTitle());
        strategyAward.setAwardSubtitle(entity.getAwardSubtitle());
        strategyAward.setAwardCount(entity.getAwardCount());
        strategyAward.setAwardCountSurplus(entity.getAwardCountSurplus());
        strategyAward.setAwardRate(entity.getAwardRate());
        strategyAward.setRuleModels(entity.getRuleModels());
        strategyAward.setSort(entity.getSort());
    }

    /**
     * 将 StrategyAward 的数据填充到已存在的 StrategyAwardEntity 对象中
     * @param strategyAward PO对象
     * @param entity 目标领域实体
     */
    public static void fillEntity(StrategyAward strategyAward, StrategyAwardEntity entity) {
        if (strategyAward == null || entity == null) {
            return;
        }
        
        entity.setStrategyId(strategyAward.getStrategyId());
        entity.setAwardId(strategyAward.getAwardId());
        entity.setAwardTitle(strategyAward.getAwardTitle());
        entity.setAwardSubtitle(strategyAward.getAwardSubtitle());
        entity.setAwardCount(strategyAward.getAwardCount());
        entity.setAwardCountSurplus(strategyAward.getAwardCountSurplus());
        entity.setAwardRate(strategyAward.getAwardRate());
        entity.setRuleModels(strategyAward.getRuleModels());
        entity.setSort(strategyAward.getSort());
    }
} 