package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.StrategyAward;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 策略奖品表数据访问层测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyAwardDaoTest {

    @Resource
    private StrategyAwardDao strategyAwardDao;
    
    private StrategyAward testStrategyAward;
    private List<Long> testRecordIds = new ArrayList<>();

    @Before
    public void setUp() {
        log.info("准备测试数据");
        testStrategyAward = new StrategyAward();
        testStrategyAward.setStrategyId(999L);
        testStrategyAward.setAwardId(101);
        testStrategyAward.setAwardTitle("测试奖品");
        testStrategyAward.setAwardSubtitle("测试奖品副标题");
        testStrategyAward.setAwardCount(1000);
        testStrategyAward.setAwardCountSurplus(1000);
        testStrategyAward.setAwardRate(new BigDecimal("0.30"));
        testStrategyAward.setSort(1);
        testStrategyAward.setCreateTime(new Date());
        testStrategyAward.setUpdateTime(new Date());
        log.info("测试数据准备完成");
    }

    @After
    public void tearDown() {
        log.info("开始清理测试数据，共{}条记录", testRecordIds.size());
        for (Long recordId : testRecordIds) {
            try {
                // 验证记录存在
                StrategyAward existing = strategyAwardDao.selectById(recordId);
                if (existing != null) {
                    int deleteResult = strategyAwardDao.deleteById(recordId);
                    if (deleteResult > 0) {
                        log.info("成功删除测试记录，ID: {}", recordId);
                    } else {
                        log.warn("删除测试记录失败，ID: {}", recordId);
                    }
                    
                    // 验证删除成功
                    StrategyAward deleted = strategyAwardDao.selectById(recordId);
                    assertNull("记录应该已被删除", deleted);
                } else {
                    log.info("测试记录不存在，ID: {}", recordId);
                }
            } catch (Exception e) {
                log.error("清理测试数据时发生异常，ID: {}", recordId, e);
            }
        }
        testRecordIds.clear();
        log.info("测试数据清理完成");
    }

    @Test
    public void testInsert() {
        log.info("开始测试插入操作");
        
        int result = strategyAwardDao.insert(testStrategyAward);
        
        assertEquals("插入操作应该成功", 1, result);
        assertNotNull("插入后ID不应该为空", testStrategyAward.getId());
        
        Long insertedId = testStrategyAward.getId();
        testRecordIds.add(insertedId);
        
        log.info("插入操作完成，生成ID: {}", insertedId);
    }

    @Test
    public void testQueryStrategyAwardListByStrategyId() {
        log.info("开始测试根据策略ID查询奖品列表操作");
        
        // 先插入测试数据
        strategyAwardDao.insert(testStrategyAward);
        testRecordIds.add(testStrategyAward.getId());
        
        List<StrategyAward> awardList = strategyAwardDao.queryStrategyAwardListByStrategyId(testStrategyAward.getStrategyId());
        
        assertNotNull("查询结果不应该为空", awardList);
        assertFalse("奖品列表不应该为空", awardList.isEmpty());
        
        // 验证是否包含我们插入的测试数据
        boolean found = awardList.stream()
                .anyMatch(award -> award.getAwardId().equals(testStrategyAward.getAwardId()) 
                        && award.getStrategyId().equals(testStrategyAward.getStrategyId()));
        assertTrue("应该能找到插入的测试数据", found);
        
        log.info("查询操作完成，找到{}个奖品", awardList.size());
    }

    @Test
    public void testUpdate() {
        log.info("开始测试更新操作");
        
        // 先插入测试数据
        strategyAwardDao.insert(testStrategyAward);
        testRecordIds.add(testStrategyAward.getId());
        
        // 更新奖品信息
        testStrategyAward.setAwardTitle("更新后的测试奖品");
        testStrategyAward.setAwardCount(2000);
        testStrategyAward.setAwardCountSurplus(2000);
        testStrategyAward.setAwardRate(new BigDecimal("0.25"));
        testStrategyAward.setUpdateTime(new Date());
        
        int result = strategyAwardDao.update(testStrategyAward);
        
        assertEquals("更新操作应该成功", 1, result);
        
        // 验证更新结果
        StrategyAward updatedAward = strategyAwardDao.selectById(testStrategyAward.getId());
        assertEquals("更新后的标题应该匹配", "更新后的测试奖品", updatedAward.getAwardTitle());
        assertEquals("更新后的数量应该匹配", Integer.valueOf(2000), updatedAward.getAwardCount());
        assertEquals("更新后的中奖率应该匹配", 0, new BigDecimal("0.25").compareTo(updatedAward.getAwardRate()));
        log.info("更新操作完成，新标题: {}", updatedAward.getAwardTitle());
    }

    @Test
    public void testCompleteWorkflow() {
        log.info("开始测试完整的CRUD工作流");
        
        // 1. 创建
        int insertResult = strategyAwardDao.insert(testStrategyAward);
        assertEquals("插入操作应该成功", 1, insertResult);
        testRecordIds.add(testStrategyAward.getId());
        log.info("步骤1 - 创建策略奖品成功，ID: {}", testStrategyAward.getId());
        
        // 2. 读取
        List<StrategyAward> awardList = strategyAwardDao.queryStrategyAwardListByStrategyId(testStrategyAward.getStrategyId());
        assertNotNull("查询结果不应该为空", awardList);
        assertFalse("奖品列表不应该为空", awardList.isEmpty());
        log.info("步骤2 - 查询策略奖品成功，共{}个奖品", awardList.size());
        
        // 3. 更新
        testStrategyAward.setAwardTitle("CRUD测试-更新后");
        testStrategyAward.setUpdateTime(new Date());
        int updateResult = strategyAwardDao.update(testStrategyAward);
        assertEquals("更新操作应该成功", 1, updateResult);
        log.info("步骤3 - 更新策略奖品成功");
        
        // 4. 验证更新
        StrategyAward updatedAward = strategyAwardDao.selectById(testStrategyAward.getId());
        assertEquals("更新后的标题应该匹配", "CRUD测试-更新后", updatedAward.getAwardTitle());
        log.info("步骤4 - 验证更新成功: {}", updatedAward.getAwardTitle());
        
        log.info("完整CRUD工作流测试成功");
    }
}
