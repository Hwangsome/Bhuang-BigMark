package com.bhuang.infrastructure.persistent.repository;

import com.alibaba.fastjson.JSON;
import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import com.bhuang.infrastructure.persistent.constants.Constants;
import com.bhuang.infrastructure.persistent.dao.StrategyAwardDao;
import com.bhuang.infrastructure.persistent.dao.StrategyRuleDao;
import com.bhuang.infrastructure.persistent.po.StrategyAward;
import com.bhuang.infrastructure.persistent.po.StrategyRule;
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
    private StrategyRuleDao strategyRuleDao;
    
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

    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        String cacheKey = Constants.Redis.getStrategyRuleKey(strategyId, ruleModel);
        StrategyRuleEntity strategyRuleEntity = redisService.get(cacheKey, StrategyRuleEntity.class);
        if (null != strategyRuleEntity) {
            return strategyRuleEntity;
        }
        
        StrategyRule strategyRule = strategyRuleDao.selectByStrategyIdAndRuleModel(strategyId, ruleModel);
        if (null == strategyRule) {
            return null;
        }
        
        strategyRuleEntity = convertToEntity(strategyRule);
        redisService.set(cacheKey, strategyRuleEntity);
        return strategyRuleEntity;
    }

    @Override
    public void storeStrategyAwardSearchRateTableByWeight(Long strategyId, String ruleWeightValue, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储权重策略概率范围
        String rangeKey = Constants.Redis.getStrategyRateRangeKeyByWeight(strategyId, ruleWeightValue);
        redisService.set(rangeKey, rateRange);
        log.info("权重策略概率范围已缓存，策略ID：{}，范围：{},rangeKey: {}", strategyId, rateRange, rangeKey);
        
        // 2. 存储权重策略奖品查找表
        strategyAwardSearchRateTable.forEach((rateKey, awardId) -> {
            String assembleKey = Constants.Redis.getStrategyAwardAssembleKeyByWeight(strategyId, ruleWeightValue, rateKey);
            redisService.set(assembleKey, awardId);
        });
    }

    @Override
    public int getRateRangeByWeight(Long strategyId, String ruleWeightValue) {
        String cacheKey = Constants.Redis.getStrategyRateRangeKeyByWeight(strategyId, ruleWeightValue);
        Integer value = redisService.get(cacheKey, Integer.class);
        return value != null ? value : 0;
    }

    @Override
    public Integer getStrategyAwardAssembleByWeight(Long strategyId, String ruleWeightValue, Integer rateKey) {
        String cacheKey = Constants.Redis.getStrategyAwardAssembleKeyByWeight(strategyId, ruleWeightValue, rateKey);
        return redisService.get(cacheKey, Integer.class);
    }
    
    /**
     * 转换PO为Entity
     */
    private StrategyRuleEntity convertToEntity(StrategyRule strategyRule) {
        if (null == strategyRule) {
            return null;
        }
        return StrategyRuleEntity.builder()
                .strategyId(strategyRule.getStrategyId().longValue())  // 转换Integer为Long
                .awardId(strategyRule.getAwardId())
                .ruleType(strategyRule.getRuleType())
                .ruleModel(strategyRule.getRuleModel())
                .ruleValue(strategyRule.getRuleValue())
                .ruleDesc(strategyRule.getRuleDesc())
                .build();
    }
}
