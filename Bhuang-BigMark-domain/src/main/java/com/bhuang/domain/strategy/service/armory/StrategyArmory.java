package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author bhuang
 * @description 策略装配库 策略装配库(兵工厂)，负责初始化策略计算
 * @create 2025-06-13
 */
@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory {

    @Resource
    private IStrategyRepository strategyRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 概率计算常量
     */
    private static final BigDecimal TEN_THOUSANDTH = new BigDecimal("0.0001");

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置，获取策略奖品列表
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardList(strategyId);
        if (strategyAwardEntityList == null || strategyAwardEntityList.isEmpty()) {
            log.warn("策略配置为空，策略ID：{}", strategyId);
            return false;
        }

        log.info("开始装配抽奖策略，策略ID：{}", strategyId);

        // 2. 获取最小概率值
        BigDecimal minAwardRate = getMinAwardRate(strategyAwardEntityList);
        log.info("最小概率值：{}", minAwardRate);

        // 3. 获取概率值总和
        BigDecimal totalAwardRate = getTotalAwardRate(strategyAwardEntityList);
        log.info("概率值总和：{}", totalAwardRate);

        // 4. 用总概率/最小概率获得概率范围
        BigDecimal rateRange = calculateRateRange(minAwardRate, totalAwardRate);
        log.info("概率范围：{}", rateRange);

        // 5. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        log.info("开始生成抽奖策略奖品概率查找表");
        List<Integer> strategyAwardSearchRateTable = generateAwardSearchTable(strategyAwardEntityList, rateRange, totalAwardRate);

        // 6. 对存储的奖品进行乱序操作。避免顺序生成的随机数前面是固定的奖品。
        log.info("开始打乱抽奖策略奖品查找表");
        shuffleAwardTable(strategyAwardSearchRateTable);

        // 7. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = generateAwardRateTableMap(strategyAwardSearchRateTable);

        // 8. 存放到 Redis
        log.info("开始存储抽奖策略数据到Redis");
        strategyRepository.storeStrategyAwardSearchRateTable(strategyId, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);

        log.info("抽奖策略装配完成，策略ID：{}", strategyId);
        return true;
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 查询策略对应的概率范围
        int rateRange = strategyRepository.getRateRange(strategyId);
        if (rateRange == 0) {
            log.warn("策略未装配或概率范围为0，策略ID：{}", strategyId);
            return null;
        }
        
        // 生成随机数，范围为 [1, rateRange]
        int randomRate = secureRandom.nextInt(rateRange) + 1;
        
        // 根据随机数查询对应的奖品ID
        Integer awardId = strategyRepository.getStrategyAwardAssemble(strategyId, randomRate);
        
        log.debug("随机抽奖结果，策略ID：{}，随机数：{}，奖品ID：{}", strategyId, randomRate, awardId);
        return awardId;
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        // TODO: 实现权重规则逻辑
        log.info("暂时使用普通随机逻辑，策略ID：{}，权重规则值：{}", strategyId, ruleWeightValue);
        return getRandomAwardId(strategyId);
    }

    @Override
    public Boolean assembleLotteryStrategyByActivityId(Long activityId) {
        // TODO: 根据活动ID装配策略
        log.info("根据活动ID装配策略，活动ID：{}", activityId);
        return false;
    }
    
    /**
     * 获取最小概率值
     * @param strategyAwardList 策略奖品列表
     * @return 最小概率值
     */
    private BigDecimal getMinAwardRate(List<StrategyAwardEntity> strategyAwardList) {
        return strategyAwardList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(TEN_THOUSANDTH);
    }
    
    /**
     * 获取概率值总和
     * @param strategyAwardList 策略奖品列表
     * @return 概率值总和
     */
    private BigDecimal getTotalAwardRate(List<StrategyAwardEntity> strategyAwardList) {
        return strategyAwardList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 计算概率范围
     * @param minAwardRate 最小奖品概率
     * @param totalAwardRate 奖品概率总和
     * @return 概率范围
     */
    private BigDecimal calculateRateRange(BigDecimal minAwardRate, BigDecimal totalAwardRate) {
        // 概率范围 = 总概率 / 最小概率值
        // 例如：总概率为100%，最小概率为0.01%，则概率范围为 100 / 0.01 = 10,000
        return totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
    }
    
    /**
     * 生成策略奖品概率查找表
     * 在list集合中，存放上对应的奖品占位，占位越多等于概率越高
     * @param strategyAwardList 策略奖品列表
     * @param rateRange 概率范围
     * @param totalAwardRate 概率总和
     * @return 奖品概率查找表
     */
    private List<Integer> generateAwardSearchTable(List<StrategyAwardEntity> strategyAwardList, BigDecimal rateRange, BigDecimal totalAwardRate) {
        List<Integer> strategyAwardSearchRateTable = new ArrayList<>();
        
        for (StrategyAwardEntity strategyAward : strategyAwardList) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            
            // 计算该奖品在查找表中的占位数量
            // 奖品概率 * 概率范围 / 概率总和 = 占位数量
            BigDecimal rateCount = awardRate
                    .multiply(rateRange)
                    .divide(totalAwardRate, 0, RoundingMode.CEILING);
            
            // 在查找表中添加对应数量的奖品ID
            for (int i = 0; i < rateCount.intValue(); i++) {
                strategyAwardSearchRateTable.add(awardId);
            }
            
            log.debug("奖品ID：{}，概率：{}%，占位数量：{}", awardId, awardRate, rateCount.intValue());
        }
        
        return strategyAwardSearchRateTable;
    }
    
    /**
     * 对奖品查找表进行乱序操作
     * 避免顺序生成的随机数前面是固定的奖品
     * @param strategyAwardSearchRateTable 奖品查找表
     */
    private void shuffleAwardTable(List<Integer> strategyAwardSearchRateTable) {
        Collections.shuffle(strategyAwardSearchRateTable, secureRandom);
    }
    
    /**
     * 生成概率空间索引到奖品ID的映射表
     * key为概率空间中的位置索引（从1开始的连续整数），每个位置被随机选中的概率相等
     * value为该位置对应的奖品ID，奖品在表中出现的次数与其概率成正比
     * @param strategyAwardSearchRateTable 已乱序的奖品查找表
     * @return 概率空间索引到奖品ID的映射
     */
    private Map<Integer, Integer> generateAwardRateTableMap(List<Integer> strategyAwardSearchRateTable) {
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new HashMap<>();
        
        for (int i = 0; i < strategyAwardSearchRateTable.size(); i++) {
            // key为位置索引（从1开始的连续整数），value为对应位置的奖品ID
            shuffleStrategyAwardSearchRateTable.put(i + 1, strategyAwardSearchRateTable.get(i));
        }
        
        return shuffleStrategyAwardSearchRateTable;
    }
}
