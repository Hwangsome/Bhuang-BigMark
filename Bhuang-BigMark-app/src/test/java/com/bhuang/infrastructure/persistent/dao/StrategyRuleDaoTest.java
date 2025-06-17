package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 策略规则表数据访问层测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyRuleDaoTest {

    @Resource
    private StrategyRuleDao strategyRuleDao;
    
    private List<Long> testRecordIds = new ArrayList<>();
    private StrategyRule testStrategyRule;

    @Before
    public void setUp() {
        log.info("========== 准备测试数据 ==========");
        // 创建测试策略规则
        testStrategyRule = new StrategyRule();
        testStrategyRule.setStrategyId(999);
        testStrategyRule.setRuleType(1); // 1-策略规则
        testStrategyRule.setRuleModel("rule_weight");
        testStrategyRule.setRuleValue("4000:102,103,104,105 6000:106,107 10000:101");
        testStrategyRule.setRuleDesc("测试权重规则-自动生成");
        testStrategyRule.setCreateTime(new Date());
        testStrategyRule.setUpdateTime(new Date());
    }

    @After
    public void tearDown() {
        log.info("========== 清理测试数据 ==========");
        // 清理所有创建的测试记录
        for (Long recordId : testRecordIds) {
            try {
                strategyRuleDao.deleteById(recordId);
                log.info("删除测试记录，ID: {}", recordId);
            } catch (Exception e) {
                log.warn("删除测试记录失败，ID: {}, 错误: {}", recordId, e.getMessage());
            }
        }
        testRecordIds.clear();
        log.info("测试数据清理完成");
    }

    @Test
    public void testInsert() {
        log.info("开始测试插入操作");
        
        int result = strategyRuleDao.insert(testStrategyRule);
        testRecordIds.add(testStrategyRule.getId());
        
        assertEquals("插入操作应该成功", 1, result);
        assertNotNull("策略规则ID不应该为空", testStrategyRule.getId());
        log.info("插入操作完成，策略规则ID: {}", testStrategyRule.getId());
    }

    @Test
    public void testSelectByStrategyIdAndRuleModel() {
        log.info("开始测试根据策略ID和规则模型查询操作");
        
        // 先插入测试数据
        strategyRuleDao.insert(testStrategyRule);
        testRecordIds.add(testStrategyRule.getId());
        
        StrategyRule selectedRule = strategyRuleDao.selectByStrategyIdAndRuleModel(
            testStrategyRule.getStrategyId().longValue(), testStrategyRule.getRuleModel());
        
        assertNotNull("查询结果不应该为空", selectedRule);
        assertEquals("策略ID应该匹配", testStrategyRule.getStrategyId(), selectedRule.getStrategyId());
        assertEquals("规则模型应该匹配", testStrategyRule.getRuleModel(), selectedRule.getRuleModel());
        assertEquals("规则值应该匹配", testStrategyRule.getRuleValue(), selectedRule.getRuleValue());
        log.info("查询操作完成，找到规则: {}", selectedRule.getRuleDesc());
    }

    @Test
    public void testUpdate() {
        log.info("开始测试更新操作");
        
        // 先插入测试数据
        strategyRuleDao.insert(testStrategyRule);
        testRecordIds.add(testStrategyRule.getId());
        
        // 更新规则描述和值
        testStrategyRule.setRuleDesc("更新后的测试规则");
        testStrategyRule.setRuleValue("5000:102,103 8000:104,105 10000:101");
        testStrategyRule.setUpdateTime(new Date());
        
        int result = strategyRuleDao.update(testStrategyRule);
        
        assertEquals("更新操作应该成功", 1, result);
        
        // 验证更新结果
        StrategyRule updatedRule = strategyRuleDao.selectByStrategyIdAndRuleModel(
            testStrategyRule.getStrategyId().longValue(), testStrategyRule.getRuleModel());
        assertEquals("更新后的描述应该匹配", "更新后的测试规则", updatedRule.getRuleDesc());
        assertEquals("更新后的规则值应该匹配", "5000:102,103 8000:104,105 10000:101", updatedRule.getRuleValue());
        log.info("更新操作完成，新描述: {}", updatedRule.getRuleDesc());
    }

    @Test
    public void testCompleteWorkflow() {
        log.info("开始测试完整的CRUD工作流");
        
        // 1. 创建
        int insertResult = strategyRuleDao.insert(testStrategyRule);
        assertEquals("插入操作应该成功", 1, insertResult);
        testRecordIds.add(testStrategyRule.getId());
        log.info("步骤1 - 创建策略规则成功，ID: {}", testStrategyRule.getId());
        
        // 2. 读取
        StrategyRule selectedRule = strategyRuleDao.selectByStrategyIdAndRuleModel(
            testStrategyRule.getStrategyId().longValue(), testStrategyRule.getRuleModel());
        assertNotNull("查询结果不应该为空", selectedRule);
        assertEquals("规则描述应该匹配", testStrategyRule.getRuleDesc(), selectedRule.getRuleDesc());
        log.info("步骤2 - 读取策略规则成功: {}", selectedRule.getRuleDesc());
        
        // 3. 更新
        selectedRule.setRuleDesc("CRUD测试-更新后");
        selectedRule.setUpdateTime(new Date());
        int updateResult = strategyRuleDao.update(selectedRule);
        assertEquals("更新操作应该成功", 1, updateResult);
        log.info("步骤3 - 更新策略规则成功");
        
        // 4. 验证更新
        StrategyRule updatedRule = strategyRuleDao.selectByStrategyIdAndRuleModel(
            testStrategyRule.getStrategyId().longValue(), testStrategyRule.getRuleModel());
        assertEquals("更新后的描述应该匹配", "CRUD测试-更新后", updatedRule.getRuleDesc());
        log.info("步骤4 - 验证更新成功: {}", updatedRule.getRuleDesc());
        
        log.info("完整CRUD工作流测试成功");
    }
}
