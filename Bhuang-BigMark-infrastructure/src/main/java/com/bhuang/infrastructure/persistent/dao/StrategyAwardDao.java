package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StrategyAwardDao {
    StrategyAward selectById(Long id);
    List<StrategyAward> selectAll();
    int insert(StrategyAward strategyAward);
    int update(StrategyAward strategyAward);
    int deleteById(Long id);
}
