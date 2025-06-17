package com.bhuang.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bhuang
 * @description 策略规则实体
 * @create 2025-06-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {

    /** 策略ID */
    private Long strategyId;
    /** 奖品ID */
    private Integer awardId;
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)、rule_weight - 权重规则】 */
    private String ruleModel;
    /** 抽奖规则比值 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;

    /**
     * 获取权重值配置映射
     * 将权重规则值解析为权重值和奖品ID列表的映射
     * 例如：6000:102,103,104,105 -> {6000: [102,103,104,105]}
     * @return 权重值配置映射
     */
    public Map<String, String[]> getRuleWeightValues() {
        Map<String, String[]> ruleWeightValues = new HashMap<>();
        if (ruleValue == null || ruleValue.isEmpty()) {
            return ruleWeightValues;
        }

        // 解析规则值，例如：4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
        String[] ruleValueGroups = ruleValue.split(" ");
        for (String ruleValueGroup : ruleValueGroups) {
            if (ruleValueGroup == null || !ruleValueGroup.contains(":")) {
                continue;
            }
            
            String[] parts = ruleValueGroup.split(":");
            if (parts.length == 2) {
                String weight = parts[0].trim();
                String[] awardIds = parts[1].trim().split(",");
                ruleWeightValues.put(weight, awardIds);
            }
        }
        
        return ruleWeightValues;
    }

    /**
     * 获取权重奖品ID数组
     * @param weightValue 权重值
     * @return 奖品ID数组
     */
    public String[] getAwardIds(String weightValue) {
        Map<String, String[]> ruleWeightValues = getRuleWeightValues();
        return ruleWeightValues.get(weightValue);
    }
} 