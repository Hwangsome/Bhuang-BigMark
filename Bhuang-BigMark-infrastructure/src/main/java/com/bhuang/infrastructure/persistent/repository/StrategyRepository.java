package com.bhuang.infrastructure.persistent.repository;

import com.alibaba.fastjson.JSON;
import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import com.bhuang.infrastructure.persistent.constants.Constants;
import com.bhuang.infrastructure.persistent.dao.StrategyAwardDao;
import com.bhuang.infrastructure.persistent.po.StrategyAward;
import com.bhuang.infrastructure.persistent.redis.IRedisService;
import com.bhuang.infrastructure.persistent.utils.StrategyAwardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 策略仓储实现
 * @author bhuang
 */
@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private StrategyAwardDao strategyAwardDao;
    
    @Resource
    private IRedisService redisService;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        String cacheKey = Constants.Redis.getStrategyAwardListKey(strategyId);
        
        // 先尝试从缓存获取
        String cacheData = redisService.get(cacheKey);
        if (cacheData != null) {
            log.info("从缓存获取策略奖品列表，策略ID：{}", strategyId);
            List<StrategyAward> strategyAwardList = JSON.parseArray(cacheData, StrategyAward.class);
            return StrategyAwardMapper.toEntityList(strategyAwardList);
        }
        
        // 缓存未命中，从数据库查询
        log.info("从数据库查询策略奖品列表，策略ID：{}", strategyId);
        List<StrategyAward> strategyAwardList = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        
        // 将查询结果缓存，设置过期时间为1小时
        redisService.set(cacheKey, JSON.toJSONString(strategyAwardList), Duration.ofSeconds(Constants.Redis.ExpireTime.ONE_HOUR));
        log.info("策略奖品列表已缓存，策略ID：{}，数量：{}", strategyId, strategyAwardList.size());
        
        // 转换并返回结果
        return StrategyAwardMapper.toEntityList(strategyAwardList);
    }

    @Override
    public void storeStrategyAwardSearchRateTable(Long strategyId, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        String cacheKey = Constants.Redis.getStrategyRateRangeKey(strategyId);
        log.info("策略奖品概率范围已缓存，策略ID：{}，范围：{}", strategyId, rateRange);
        redisService.set(cacheKey, rateRange);
        
        for (Map.Entry<Integer, Integer> entry : strategyAwardSearchRateTable.entrySet()) {
            String assembleKey = Constants.Redis.getStrategyAwardAssembleKey(strategyId, entry.getKey());
            redisService.set(assembleKey, entry.getValue());
        }
    }

    @Override
    public int getRateRange(Long strategyId) {
        String cacheKey = Constants.Redis.getStrategyRateRangeKey(strategyId);
        Integer value = redisService.get(cacheKey);
        return value != null ? value : 0;
    }

    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, Integer rateKey) {
        String cacheKey = Constants.Redis.getStrategyAwardAssembleKey(strategyId, rateKey);
        Integer value = redisService.get(cacheKey);
        return value;
    }
}
