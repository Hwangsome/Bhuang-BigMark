package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StrategyRuleDao {
    StrategyRule selectById(Long id);
    List<StrategyRule> selectAll();
    int insert(StrategyRule strategyRule);
    int update(StrategyRule strategyRule);
    int deleteById(Long id);
    
    /**
     * 根据策略ID和规则模型查询策略规则
     * @param strategyId 策略ID
     * @param ruleModel 规则模型
     * @return 策略规则对象
     */
    StrategyRule selectByStrategyIdAndRuleModel(@Param("strategyId") Long strategyId, 
                                               @Param("ruleModel") String ruleModel);
}
