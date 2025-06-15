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
