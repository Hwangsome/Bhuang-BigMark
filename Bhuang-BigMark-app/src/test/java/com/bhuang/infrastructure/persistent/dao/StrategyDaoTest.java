package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Strategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyDaoTest {
    @Autowired
    private StrategyDao strategyDao;

    @Test
    public void testInsertAndSelect() {
        Strategy strategy = new Strategy();
        strategy.setStrategyId(888L);
        strategy.setStrategyDesc("测试策略");
        
        int result = strategyDao.insert(strategy);
        assertEquals(1, result);
        
        List<Strategy> list = strategyDao.selectAll();
        assertTrue(list.stream().anyMatch(s -> "测试策略".equals(s.getStrategyDesc())));
    }

    @Test
    public void testUpdateAndDelete() {
        List<Strategy> list = strategyDao.selectAll();
        Strategy strategy = list.get(0);
        strategy.setStrategyDesc("更新描述");
        int update = strategyDao.update(strategy);
        assertEquals(1, update);
        int delete = strategyDao.deleteById(strategy.getId());
        assertEquals(1, delete);
    }
}
