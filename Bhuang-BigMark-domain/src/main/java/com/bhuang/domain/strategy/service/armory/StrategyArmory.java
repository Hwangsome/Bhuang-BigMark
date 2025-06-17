package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author bhuang
 * @description 策略装配库 策略装配库(兵工厂)，负责初始化策略计算
 * @create 2025-06-13
 */
@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory, IStrategyAssemble, IStrategyDispatch {

    @Resource
    private IStrategyRepository strategyRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 概率计算常量
     */
    private static final BigDecimal TEN_THOUSANDTH = new BigDecimal("0.0001");

    // ==================== 装配接口实现 ====================
    
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardList(strategyId);
        if (strategyAwardEntityList == null || strategyAwardEntityList.isEmpty()) {
            log.warn("策略配置为空，策略ID：{}", strategyId);
            return false;
        }

        log.info("开始装配抽奖策略，策略ID：{}", strategyId);

        // 2. 装配正常策略（无权重）- 这是必需的
        boolean normalResult = assembleNormalLotteryStrategy(strategyId, strategyAwardEntityList);
        if (!normalResult) {
            log.error("正常策略装配失败，策略ID：{}", strategyId);
            return false;
        }

        // 3. 检查是否存在权重规则
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRule(strategyId, "rule_weight");
        boolean hasWeightRule = (strategyRuleEntity != null);

        // 4. 装配权重策略
        boolean weightResult = assembleWeightLotteryStrategy(strategyId, strategyAwardEntityList, strategyRuleEntity);

        log.info("抽奖策略装配完成，策略ID：{}，正常策略：{}，权重策略：{}，存在权重规则：{}", 
                strategyId, normalResult, weightResult, hasWeightRule);
        
        // 5. 确定最终结果
        // - 正常策略必须成功
        // - 如果存在权重规则，权重策略也必须成功
        // - 如果不存在权重规则，权重策略结果无关紧要
        boolean finalResult = normalResult && (!hasWeightRule || weightResult);
        
        if (!finalResult) {
            if (hasWeightRule && !weightResult) {
                log.error("策略装配失败：存在权重规则但权重策略装配失败，策略ID：{}", strategyId);
            }
        }
        
        return finalResult;
    }
    
    @Override
    public Boolean assembleLotteryStrategyByActivityId(Long activityId) {
        log.info("根据活动ID装配策略，活动ID：{}", activityId);
        // 注意：此方法需要在未来与活动模块集成时实现
        // 目前返回false表示功能暂未实现
        log.warn("根据活动ID装配策略功能暂未实现，活动ID：{}", activityId);
        return false;
    }

    // ==================== 调度接口实现 ====================
    
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
        log.info("执行权重抽奖，策略ID：{}，权重规则值：{}", strategyId, ruleWeightValue);

        // 查询权重策略对应的概率范围
        int rateRange = strategyRepository.getRateRangeByWeight(strategyId, ruleWeightValue);
        if (rateRange == 0) {
            log.warn("权重策略未装配或概率范围为0，策略ID：{}，权重值：{}，使用普通策略", strategyId, ruleWeightValue);
            return getRandomAwardId(strategyId);
        }
        
        // 生成随机数，范围为 [1, rateRange]
        int randomRate = secureRandom.nextInt(rateRange) + 1;
        
        // 根据随机数查询对应的奖品ID
        Integer awardId = strategyRepository.getStrategyAwardAssembleByWeight(strategyId, ruleWeightValue, randomRate);
        
        log.info("权重抽奖结果，策略ID：{}，权重值：{}，随机数：{}，奖品ID：{}", strategyId, ruleWeightValue, randomRate, awardId);
        return awardId;
    }

    // ==================== 私有方法 ====================

    /**
     * 装配正常策略（无权重）
     * @param strategyId 策略ID
     * @param strategyAwardEntityList 策略奖品列表
     * @return 装配结果
     */
    private boolean assembleNormalLotteryStrategy(Long strategyId, List<StrategyAwardEntity> strategyAwardEntityList) {
        log.info("开始装配正常抽奖策略，策略ID：{}", strategyId);

        // 获取最小概率值
        BigDecimal minAwardRate = getMinAwardRate(strategyAwardEntityList);
        log.info("最小概率值：{}", minAwardRate);

        // 获取概率值总和
        BigDecimal totalAwardRate = getTotalAwardRate(strategyAwardEntityList);
        log.info("概率值总和：{}", totalAwardRate);

        // 用总概率/最小概率获得概率范围
        BigDecimal rateRange = calculateRateRange(minAwardRate, totalAwardRate);
        log.info("概率范围：{}", rateRange);

        // 生成策略奖品概率查找表
        log.info("开始生成抽奖策略奖品概率查找表");
        List<Integer> strategyAwardSearchRateTable = generateAwardSearchTable(strategyAwardEntityList, rateRange, totalAwardRate);

        // 对存储的奖品进行乱序操作
        log.info("开始打乱抽奖策略奖品查找表");
        shuffleAwardTable(strategyAwardSearchRateTable);

        // 生成出Map集合，key值，对应的就是后续的概率值
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = generateAwardRateTableMap(strategyAwardSearchRateTable);

        // 存放到 Redis
        log.info("开始存储抽奖策略数据到Redis");
        strategyRepository.storeStrategyAwardSearchRateTable(strategyId, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);

        log.info("正常抽奖策略装配完成，策略ID：{}", strategyId);
        return true;
    }

    /**
     * 装配权重策略
     * @param strategyId 策略ID
     * @param strategyAwardEntityList 策略奖品列表
     * @param strategyRuleEntity 权重规则
     * @return 装配结果
     */
    private boolean assembleWeightLotteryStrategy(Long strategyId, List<StrategyAwardEntity> strategyAwardEntityList, StrategyRuleEntity strategyRuleEntity) {
        log.info("开始装配权重抽奖策略，策略ID：{}", strategyId);

        if (strategyRuleEntity == null) {
            log.info("策略权重规则为空，跳过权重策略装配，策略ID：{}", strategyId);
            return true;
        }

        log.info("查询到权重规则，策略ID：{}，规则值：{}", strategyId, strategyRuleEntity.getRuleValue());

        // 解析权重规则值
        Map<String, String[]> ruleWeightValues = strategyRuleEntity.getRuleWeightValues();
        
        for (Map.Entry<String, String[]> entry : ruleWeightValues.entrySet()) {
            String ruleWeightValue = entry.getKey();
            String[] awardIds = entry.getValue();
            
            log.info("装配权重策略，权重值：{}，奖品ID：{}", ruleWeightValue, Arrays.toString(awardIds));

            // 过滤出权重范围内的奖品
            List<Integer> awardIdList = Arrays.stream(awardIds)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<StrategyAwardEntity> weightStrategyAwardList = strategyAwardEntityList.stream()
                    .filter(strategyAward -> awardIdList.contains(strategyAward.getAwardId()))
                    .collect(Collectors.toList());

            if (weightStrategyAwardList.isEmpty()) {
                log.warn("权重范围内无奖品，权重值：{}", ruleWeightValue);
                continue;
            }

            // 重新计算权重范围内奖品的概率
            adjustAwardRatesForWeight(weightStrategyAwardList);

            // 装配权重策略
            assembleWeightStrategy(strategyId, ruleWeightValue, weightStrategyAwardList);
        }

        log.info("权重抽奖策略装配完成，策略ID：{}", strategyId);
        return true;
    }

    /**
     * 调整权重范围内奖品的概率
     * @param weightStrategyAwardList 权重范围内的奖品列表
     */
    private void adjustAwardRatesForWeight(List<StrategyAwardEntity> weightStrategyAwardList) {
        BigDecimal totalRate = weightStrategyAwardList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按原比例重新分配概率，使总概率为100%
        BigDecimal hundred = new BigDecimal("100");
        for (StrategyAwardEntity strategyAward : weightStrategyAwardList) {
            BigDecimal adjustedRate = strategyAward.getAwardRate()
                    .multiply(hundred)
                    .divide(totalRate, 4, RoundingMode.HALF_UP);
            strategyAward.setAwardRate(adjustedRate);
        }
    }

    /**
     * 装配权重策略
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @param weightStrategyAwardList 权重范围内的奖品列表
     */
    private void assembleWeightStrategy(Long strategyId, String ruleWeightValue, List<StrategyAwardEntity> weightStrategyAwardList) {
        // 获取最小概率值
        BigDecimal minAwardRate = getMinAwardRate(weightStrategyAwardList);
        // 获取概率值总和
        BigDecimal totalAwardRate = getTotalAwardRate(weightStrategyAwardList);
        // 计算概率范围
        BigDecimal rateRange = calculateRateRange(minAwardRate, totalAwardRate);

        // 生成策略奖品概率查找表
        List<Integer> strategyAwardSearchRateTable = generateAwardSearchTable(weightStrategyAwardList, rateRange, totalAwardRate);
        
        // 打乱查找表
        shuffleAwardTable(strategyAwardSearchRateTable);
        
        // 生成映射表
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = generateAwardRateTableMap(strategyAwardSearchRateTable);

        // 存储权重策略数据
        strategyRepository.storeStrategyAwardSearchRateTableByWeight(strategyId, ruleWeightValue, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
        
        log.info("权重策略装配完成，策略ID：{}，权重值：{}，概率范围：{}", strategyId, ruleWeightValue, rateRange);
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
        return totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
    }
    
    /**
     * 生成策略奖品概率查找表
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
     * @param strategyAwardSearchRateTable 奖品查找表
     */
    private void shuffleAwardTable(List<Integer> strategyAwardSearchRateTable) {
        Collections.shuffle(strategyAwardSearchRateTable, secureRandom);
    }
    
    /**
     * 生成概率空间索引到奖品ID的映射表
     * @param strategyAwardSearchRateTable 已乱序的奖品查找表
     * @return 概率空间索引到奖品ID的映射
     */
    private Map<Integer, Integer> generateAwardRateTableMap(List<Integer> strategyAwardSearchRateTable) {
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new HashMap<>();
        
        for (int i = 0; i < strategyAwardSearchRateTable.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i + 1, strategyAwardSearchRateTable.get(i));
        }
        
        return shuffleStrategyAwardSearchRateTable;
    }
}
