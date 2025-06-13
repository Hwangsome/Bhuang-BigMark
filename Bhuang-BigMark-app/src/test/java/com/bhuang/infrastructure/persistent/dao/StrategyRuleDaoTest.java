package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyRule;
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
public class StrategyRuleDaoTest {
    @Autowired
    private StrategyRuleDao strategyRuleDao;

    @Test
    public void testInsertAndSelect() {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(666);
        strategyRule.setAwardId(777);
        strategyRule.setRuleType(1);
        strategyRule.setRuleModel("test_model");
        strategyRule.setRuleValue("test_value");
        strategyRule.setRuleDesc("测试规则");
        
        int result = strategyRuleDao.insert(strategyRule);
        assertEquals(1, result);
        
        List<StrategyRule> list = strategyRuleDao.selectAll();
        assertTrue(list.stream().anyMatch(sr -> "测试规则".equals(sr.getRuleDesc())));
    }

    @Test
    public void testUpdateAndDelete() {
        List<StrategyRule> list = strategyRuleDao.selectAll();
        StrategyRule strategyRule = list.get(0);
        strategyRule.setRuleDesc("更新规则");
        int update = strategyRuleDao.update(strategyRule);
        assertEquals(1, update);
        int delete = strategyRuleDao.deleteById(strategyRule.getId());
        assertEquals(1, delete);
    }
}
