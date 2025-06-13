package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Award;
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
public class AwardDaoTest {
    @Autowired
    private AwardDao awardDao;

    @Test
    public void testInsertAndSelect() {
        Award award = new Award();
        award.setAwardId(999);
        award.setAwardKey("test_key");
        award.setAwardConfig("test_config");
        award.setAwardDesc("测试奖品");
        
        int result = awardDao.insert(award);
        assertEquals(1, result);
        
        List<Award> list = awardDao.selectAll();
        assertTrue(list.stream().anyMatch(a -> "test_key".equals(a.getAwardKey())));
    }

    @Test
    public void testUpdateAndDelete() {
        List<Award> list = awardDao.selectAll();
        Award award = list.get(0);
        award.setAwardDesc("更新描述");
        int update = awardDao.update(award);
        assertEquals(1, update);
        int delete = awardDao.deleteById(award.getId());
        assertEquals(1, delete);
    }
}
