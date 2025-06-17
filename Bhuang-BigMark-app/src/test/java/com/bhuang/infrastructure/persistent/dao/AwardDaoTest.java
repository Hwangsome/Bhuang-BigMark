package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Award;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardDaoTest {
    
    @Autowired
    private AwardDao awardDao;
    
    private List<Integer> testRecordIds = new ArrayList<>();
    private Award testAward;

    @Before
    public void setUp() {
        log.info("开始准备AwardDao测试数据...");
        testRecordIds.clear();
        
        // 创建测试奖品数据
        testAward = new Award();
        testAward.setAwardId(999888);  // 使用特殊ID避免与现有数据冲突
        testAward.setAwardKey("test_award_key_" + System.currentTimeMillis());
        testAward.setAwardConfig("test_config");
        testAward.setAwardDesc("测试奖品描述");
        
        log.info("测试数据准备完成");
    }

    @After
    public void tearDown() {
        log.info("开始清理AwardDao测试数据...");
        
        // 清理所有创建的测试记录
        for (Integer recordId : testRecordIds) {
            try {
                awardDao.deleteById(recordId);
                log.info("删除测试记录，ID: {}", recordId);
            } catch (Exception e) {
                log.warn("删除测试记录失败，ID: {}, 错误: {}", recordId, e.getMessage());
            }
        }
        
        // 额外清理：根据特殊标识清理可能遗留的测试数据
        try {
            List<Award> allAwards = awardDao.selectAll();
            for (Award award : allAwards) {
                if (award.getAwardKey() != null && award.getAwardKey().startsWith("test_award_key_")) {
                    awardDao.deleteById(award.getId());
                    log.info("清理遗留测试数据，ID: {}, Key: {}", award.getId(), award.getAwardKey());
                }
            }
        } catch (Exception e) {
            log.warn("清理遗留测试数据时出错: {}", e.getMessage());
        }
        
        testRecordIds.clear();
        log.info("AwardDao测试数据清理完成");
    }

    @Test
    public void testInsert() {
        log.info("=== 测试奖品插入功能 ===");
        
        // 执行插入操作
        int result = awardDao.insert(testAward);
        assertEquals("插入操作应该成功", 1, result);
        
        // 记录插入的ID用于后续清理
        assertNotNull("插入后应该有自增ID", testAward.getId());
        testRecordIds.add(testAward.getId());
        
        log.info("奖品插入成功，ID: {}, AwardId: {}", testAward.getId(), testAward.getAwardId());
    }

    @Test
    public void testSelect() {
        log.info("=== 测试奖品查询功能 ===");
        
        // 先插入测试数据
        int insertResult = awardDao.insert(testAward);
        assertEquals("插入操作应该成功", 1, insertResult);
        testRecordIds.add(testAward.getId());
        
        // 测试查询所有
        List<Award> allAwards = awardDao.selectAll();
        assertNotNull("查询结果不应该为null", allAwards);
        assertFalse("查询结果不应该为空", allAwards.isEmpty());
        
        // 验证我们插入的数据存在
        boolean found = allAwards.stream()
                .anyMatch(award -> testAward.getAwardKey().equals(award.getAwardKey()));
        assertTrue("应该能查询到刚插入的测试数据", found);
        
        log.info("奖品查询测试通过，总数量: {}", allAwards.size());
    }

    @Test
    public void testUpdate() {
        log.info("=== 测试奖品更新功能 ===");
        
        // 先插入测试数据
        int insertResult = awardDao.insert(testAward);
        assertEquals("插入操作应该成功", 1, insertResult);
        testRecordIds.add(testAward.getId());
        
        // 更新数据
        String originalDesc = testAward.getAwardDesc();
        String newDesc = "更新后的奖品描述_" + System.currentTimeMillis();
        testAward.setAwardDesc(newDesc);
        
        int updateResult = awardDao.update(testAward);
        assertEquals("更新操作应该成功", 1, updateResult);
        
        // 验证更新结果
        List<Award> allAwards = awardDao.selectAll();
        Award updatedAward = allAwards.stream()
                .filter(award -> testAward.getId().equals(award.getId()))
                .findFirst()
                .orElse(null);
        
        assertNotNull("更新后应该能查到数据", updatedAward);
        assertEquals("描述应该已更新", newDesc, updatedAward.getAwardDesc());
        assertNotEquals("描述应该与原值不同", originalDesc, updatedAward.getAwardDesc());
        
        log.info("奖品更新测试通过，原描述: {}, 新描述: {}", originalDesc, newDesc);
    }

    @Test
    public void testDelete() {
        log.info("=== 测试奖品删除功能 ===");
        
        // 先插入测试数据
        int insertResult = awardDao.insert(testAward);
        assertEquals("插入操作应该成功", 1, insertResult);
        Integer insertedId = testAward.getId();
        
        // 验证数据存在
        List<Award> beforeDelete = awardDao.selectAll();
        boolean existsBeforeDelete = beforeDelete.stream()
                .anyMatch(award -> insertedId.equals(award.getId()));
        assertTrue("删除前数据应该存在", existsBeforeDelete);
        
        // 执行删除操作
        int deleteResult = awardDao.deleteById(insertedId);
        assertEquals("删除操作应该成功", 1, deleteResult);
        
        // 验证删除结果
        List<Award> afterDelete = awardDao.selectAll();
        boolean existsAfterDelete = afterDelete.stream()
                .anyMatch(award -> insertedId.equals(award.getId()));
        assertFalse("删除后数据不应该存在", existsAfterDelete);
        
        // 从清理列表中移除（因为已经删除了）
        testRecordIds.remove(insertedId);
        
        log.info("奖品删除测试通过，删除ID: {}", insertedId);
    }

    @Test
    public void testCompleteWorkflow() {
        log.info("=== 测试奖品完整工作流程 ===");
        
        // 1. 插入
        int insertResult = awardDao.insert(testAward);
        assertEquals("插入应该成功", 1, insertResult);
        testRecordIds.add(testAward.getId());
        
        // 2. 查询验证
        List<Award> awards = awardDao.selectAll();
        Award foundAward = awards.stream()
                .filter(award -> testAward.getId().equals(award.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull("应该能查询到插入的数据", foundAward);
        assertEquals("奖品ID应该匹配", testAward.getAwardId(), foundAward.getAwardId());
        
        // 3. 更新
        String newDesc = "完整流程测试_更新描述";
        foundAward.setAwardDesc(newDesc);
        int updateResult = awardDao.update(foundAward);
        assertEquals("更新应该成功", 1, updateResult);
        
        // 4. 验证更新
        awards = awardDao.selectAll();
        Award updatedAward = awards.stream()
                .filter(award -> testAward.getId().equals(award.getId()))
                .findFirst()
                .orElse(null);
        assertEquals("更新后的描述应该匹配", newDesc, updatedAward.getAwardDesc());
        
        log.info("奖品完整工作流程测试通过");
    }
}
