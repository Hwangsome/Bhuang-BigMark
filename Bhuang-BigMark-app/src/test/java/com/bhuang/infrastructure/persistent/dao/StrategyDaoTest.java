package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Strategy;
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
 * 策略表数据访问层测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyDaoTest {

    @Resource
    private StrategyDao strategyDao;
    
    private List<Long> testRecordIds = new ArrayList<>();
    private Strategy testStrategy;

    @Before
    public void setUp() {
        log.info("========== 准备测试数据 ==========");
        // 创建测试策略
        testStrategy = new Strategy();
        testStrategy.setStrategyId(999L);
        testStrategy.setStrategyDesc("测试策略-自动生成");
        testStrategy.setRuleModel("single");
        testStrategy.setCreateTime(new Date());
        testStrategy.setUpdateTime(new Date());
    }

    @After
    public void tearDown() {
        log.info("========== 清理测试数据 ==========");
        // 清理所有创建的测试记录
        for (Long recordId : testRecordIds) {
            try {
                strategyDao.deleteById(recordId);
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
        
        int result = strategyDao.insert(testStrategy);
        testRecordIds.add(testStrategy.getId());
        
        assertEquals("插入操作应该成功", 1, result);
        assertNotNull("策略ID不应该为空", testStrategy.getId());
        log.info("插入操作完成，策略ID: {}", testStrategy.getId());
    }

    @Test
    public void testSelectById() {
        log.info("开始测试根据ID查询操作");
        
        // 先插入测试数据
        strategyDao.insert(testStrategy);
        testRecordIds.add(testStrategy.getId());
        
        Strategy selectedStrategy = strategyDao.selectById(testStrategy.getId());
        
        assertNotNull("查询结果不应该为空", selectedStrategy);
        assertEquals("策略ID应该匹配", testStrategy.getStrategyId(), selectedStrategy.getStrategyId());
        assertEquals("策略描述应该匹配", testStrategy.getStrategyDesc(), selectedStrategy.getStrategyDesc());
        log.info("查询操作完成，找到策略: {}", selectedStrategy.getStrategyDesc());
    }

    @Test
    public void testUpdate() {
        log.info("开始测试更新操作");
        
        // 先插入测试数据
        strategyDao.insert(testStrategy);
        testRecordIds.add(testStrategy.getId());
        
        // 更新策略描述
        testStrategy.setStrategyDesc("更新后的测试策略");
        testStrategy.setUpdateTime(new Date());
        
        int result = strategyDao.update(testStrategy);
        
        assertEquals("更新操作应该成功", 1, result);
        
        // 验证更新结果
        Strategy updatedStrategy = strategyDao.selectById(testStrategy.getId());
        assertEquals("更新后的描述应该匹配", "更新后的测试策略", updatedStrategy.getStrategyDesc());
        log.info("更新操作完成，新描述: {}", updatedStrategy.getStrategyDesc());
    }

    @Test
    public void testCompleteWorkflow() {
        log.info("开始测试完整的CRUD工作流");
        
        // 1. 创建
        int insertResult = strategyDao.insert(testStrategy);
        assertEquals("插入操作应该成功", 1, insertResult);
        testRecordIds.add(testStrategy.getId());
        log.info("步骤1 - 创建策略成功，ID: {}", testStrategy.getId());
        
        // 2. 读取
        Strategy selectedStrategy = strategyDao.selectById(testStrategy.getId());
        assertNotNull("查询结果不应该为空", selectedStrategy);
        assertEquals("策略描述应该匹配", testStrategy.getStrategyDesc(), selectedStrategy.getStrategyDesc());
        log.info("步骤2 - 读取策略成功: {}", selectedStrategy.getStrategyDesc());
        
        // 3. 更新
        selectedStrategy.setStrategyDesc("CRUD测试-更新后");
        selectedStrategy.setUpdateTime(new Date());
        int updateResult = strategyDao.update(selectedStrategy);
        assertEquals("更新操作应该成功", 1, updateResult);
        log.info("步骤3 - 更新策略成功");
        
        // 4. 验证更新
        Strategy updatedStrategy = strategyDao.selectById(testStrategy.getId());
        assertEquals("更新后的描述应该匹配", "CRUD测试-更新后", updatedStrategy.getStrategyDesc());
        log.info("步骤4 - 验证更新成功: {}", updatedStrategy.getStrategyDesc());
        
        log.info("完整CRUD工作流测试成功");
    }
}
