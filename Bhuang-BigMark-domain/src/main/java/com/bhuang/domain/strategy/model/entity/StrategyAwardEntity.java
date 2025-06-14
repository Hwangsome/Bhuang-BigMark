package com.bhuang.domain.strategy.model.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 策略奖品实体
 * @author bhuang
 */
public class StrategyAwardEntity {
    
    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID - 内部流转使用 */
    private Integer awardId;
    /** 抽奖奖品标题 */
    private String awardTitle;
    /** 抽奖奖品副标题 */
    private String awardSubtitle;
    /** 奖品库存总量 */
    private Integer awardCount;
    /** 奖品库存剩余 */
    private Integer awardCountSurplus;
    /** 奖品中奖概率 */
    private BigDecimal awardRate;
    /** 规则模型，rule配置的模型同步到此表，便于使用 */
    private String ruleModels;
    /** 排序 */
    private Integer sort;

    // 构造函数
    public StrategyAwardEntity() {}
    
    public StrategyAwardEntity(Long strategyId, Integer awardId, String awardTitle, String awardSubtitle, 
                              Integer awardCount, Integer awardCountSurplus, BigDecimal awardRate, 
                              String ruleModels, Integer sort) {
        this.strategyId = strategyId;
        this.awardId = awardId;
        this.awardTitle = awardTitle;
        this.awardSubtitle = awardSubtitle;
        this.awardCount = awardCount;
        this.awardCountSurplus = awardCountSurplus;
        this.awardRate = awardRate;
        this.ruleModels = ruleModels;
        this.sort = sort;
    }

    /**
     * 从StrategyAward PO对象创建领域实体
     * @param strategyAward PO对象
     * @return 领域实体
     */
    public static StrategyAwardEntity fromStrategyAward(Object strategyAward) {
        if (strategyAward == null) {
            return null;
        }
        
        try {
            // 使用反射获取PO对象的属性值，避免直接依赖infrastructure层
            Class<?> clazz = strategyAward.getClass();
            StrategyAwardEntity entity = new StrategyAwardEntity();
            
            entity.setStrategyId((Long) getFieldValue(clazz, strategyAward, "strategyId"));
            entity.setAwardId((Integer) getFieldValue(clazz, strategyAward, "awardId"));
            entity.setAwardTitle((String) getFieldValue(clazz, strategyAward, "awardTitle"));
            entity.setAwardSubtitle((String) getFieldValue(clazz, strategyAward, "awardSubtitle"));
            entity.setAwardCount((Integer) getFieldValue(clazz, strategyAward, "awardCount"));
            entity.setAwardCountSurplus((Integer) getFieldValue(clazz, strategyAward, "awardCountSurplus"));
            entity.setAwardRate((BigDecimal) getFieldValue(clazz, strategyAward, "awardRate"));
            entity.setRuleModels((String) getFieldValue(clazz, strategyAward, "ruleModels"));
            entity.setSort((Integer) getFieldValue(clazz, strategyAward, "sort"));
            
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("转换StrategyAward到StrategyAwardEntity失败", e);
        }
    }

    
    /**
     * 反射工具方法：获取字段值
     */
    private static Object getFieldValue(Class<?> clazz, Object obj, String fieldName) throws Exception {
        try {
            // 先尝试直接访问字段
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            // 如果字段不存在，尝试使用getter方法
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            java.lang.reflect.Method getter = clazz.getMethod(getterName);
            return getter.invoke(obj);
        }
    }
    
    /**
     * 将当前实体数据填充到StrategyAward PO对象中
     * @param strategyAward 目标PO对象
     */
    public void toStrategyAward(Object strategyAward) {
        if (strategyAward == null) {
            return;
        }
        
        try {
            // 使用反射设置PO对象的属性值，避免直接依赖infrastructure层
            Class<?> clazz = strategyAward.getClass();
            
            setFieldValue(clazz, strategyAward, "strategyId", this.strategyId);
            setFieldValue(clazz, strategyAward, "awardId", this.awardId);
            setFieldValue(clazz, strategyAward, "awardTitle", this.awardTitle);
            setFieldValue(clazz, strategyAward, "awardSubtitle", this.awardSubtitle);
            setFieldValue(clazz, strategyAward, "awardCount", this.awardCount);
            setFieldValue(clazz, strategyAward, "awardCountSurplus", this.awardCountSurplus);
            setFieldValue(clazz, strategyAward, "awardRate", this.awardRate);
            setFieldValue(clazz, strategyAward, "ruleModels", this.ruleModels);
            setFieldValue(clazz, strategyAward, "sort", this.sort);
            
        } catch (Exception e) {
            throw new RuntimeException("转换StrategyAwardEntity到StrategyAward失败", e);
        }
    }

    /**
     * 反射工具方法：设置字段值
     */
    private static void setFieldValue(Class<?> clazz, Object obj, String fieldName, Object value) throws Exception {
        try {
            // 先尝试直接访问字段
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            // 如果字段不存在，尝试使用setter方法
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            java.lang.reflect.Method setter = clazz.getMethod(setterName, value != null ? value.getClass() : Object.class);
            setter.invoke(obj, value);
        }
    }

    // Getter and Setter methods
    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public Integer getAwardId() {
        return awardId;
    }

    public void setAwardId(Integer awardId) {
        this.awardId = awardId;
    }

    public String getAwardTitle() {
        return awardTitle;
    }

    public void setAwardTitle(String awardTitle) {
        this.awardTitle = awardTitle;
    }

    public String getAwardSubtitle() {
        return awardSubtitle;
    }

    public void setAwardSubtitle(String awardSubtitle) {
        this.awardSubtitle = awardSubtitle;
    }

    public Integer getAwardCount() {
        return awardCount;
    }

    public void setAwardCount(Integer awardCount) {
        this.awardCount = awardCount;
    }

    public Integer getAwardCountSurplus() {
        return awardCountSurplus;
    }

    public void setAwardCountSurplus(Integer awardCountSurplus) {
        this.awardCountSurplus = awardCountSurplus;
    }

    public BigDecimal getAwardRate() {
        return awardRate;
    }

    public void setAwardRate(BigDecimal awardRate) {
        this.awardRate = awardRate;
    }

    public String getRuleModels() {
        return ruleModels;
    }

    public void setRuleModels(String ruleModels) {
        this.ruleModels = ruleModels;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "StrategyAwardEntity{" +
                "strategyId=" + strategyId +
                ", awardId=" + awardId +
                ", awardTitle='" + awardTitle + '\'' +
                ", awardSubtitle='" + awardSubtitle + '\'' +
                ", awardCount=" + awardCount +
                ", awardCountSurplus=" + awardCountSurplus +
                ", awardRate=" + awardRate +
                ", ruleModels='" + ruleModels + '\'' +
                ", sort=" + sort +
                '}';
    }
}
