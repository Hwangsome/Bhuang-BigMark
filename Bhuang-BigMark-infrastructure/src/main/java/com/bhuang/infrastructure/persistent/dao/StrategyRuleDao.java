package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StrategyRuleDao {
    StrategyRule selectById(Long id);
    List<StrategyRule> selectAll();
    int insert(StrategyRule strategyRule);
    int update(StrategyRule strategyRule);
    int deleteById(Long id);
}
