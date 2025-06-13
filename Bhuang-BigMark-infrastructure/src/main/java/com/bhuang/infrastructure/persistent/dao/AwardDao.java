package com.bhuang.infrastructure.persistent.dao;

import com.bhuang.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AwardDao {
    Award selectById(Integer id);
    List<Award> selectAll();
    int insert(Award award);
    int update(Award award);
    int deleteById(Integer id);
}
