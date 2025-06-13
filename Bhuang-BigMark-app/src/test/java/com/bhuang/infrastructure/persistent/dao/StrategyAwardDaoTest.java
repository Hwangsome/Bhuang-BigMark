package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyAward;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyAwardDaoTest {
    @Autowired
    private StrategyAwardDao strategyAwardDao;

    @Test
    public void testInsertAndSelect() {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(777L);
        strategyAward.setAwardId(888);
        strategyAward.setAwardTitle("测试奖品");
        strategyAward.setAwardSubtitle("测试副标题");
        strategyAward.setAwardCount(100);
        strategyAward.setAwardCountSurplus(100);
        strategyAward.setAwardRate(new BigDecimal("0.1"));
        strategyAward.setRuleModels("test_rule");
        strategyAward.setSort(1);
        
        int result = strategyAwardDao.insert(strategyAward);
        assertEquals(1, result);
        
        List<StrategyAward> list = strategyAwardDao.selectAll();
        assertTrue(list.stream().anyMatch(sa -> "测试奖品".equals(sa.getAwardTitle())));
    }

    @Test
    public void testUpdateAndDelete() {
        List<StrategyAward> list = strategyAwardDao.selectAll();
        StrategyAward strategyAward = list.get(0);
        strategyAward.setAwardTitle("更新标题");
        int update = strategyAwardDao.update(strategyAward);
        assertEquals(1, update);
        int delete = strategyAwardDao.deleteById(strategyAward.getId());
        assertEquals(1, delete);
    }
}
