package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Strategy;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StrategyDao {
    Strategy selectById(Long id);
    List<Strategy> selectAll();
    int insert(Strategy strategy);
    int update(Strategy strategy);
    int deleteById(Long id);
}
