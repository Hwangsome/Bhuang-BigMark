# 📚 API 文档

> 本文档详细介绍BigMark抽奖策略系统的核心API接口、使用方法和示例代码。

## 📋 目录

1. [🎯 核心接口概览](#-核心接口概览)
2. [⚙️ 策略装配API](#️-策略装配api)
3. [🎲 抽奖调度API](#-抽奖调度api)
4. [💾 数据仓储API](#-数据仓储api)
5. [🔴 Redis缓存API](#-redis缓存api)
6. [📊 实体类说明](#-实体类说明)
7. [🧪 使用示例](#-使用示例)

---

## 🎯 核心接口概览

BigMark抽奖策略系统提供以下核心API接口：

| 接口类型 | 接口名称 | 主要功能 | 所在层次 |
|----------|----------|----------|----------|
| **策略装配** | `IStrategyAssemble` | 策略装配入口 | 领域层 |
| **策略装配库** | `IStrategyArmory` | 策略装配核心逻辑 | 领域层 |
| **策略调度** | `IStrategyDispatch` | 抽奖执行调度 | 领域层 |
| **数据仓储** | `IStrategyRepository` | 数据访问抽象 | 领域层 |
| **Redis服务** | `IRedisService` | 缓存操作服务 | 基础设施层 |

---

## ⚙️ 策略装配API

### 📋 IStrategyAssemble 接口

策略装配服务的入口接口，提供策略装配的统一门面。

```java
package com.bhuang.domain.strategy.service.armory;

/**
 * 策略装配服务接口
 * 提供抽奖策略的装配功能
 */
public interface IStrategyAssemble {
    
    /**
     * 装配抽奖策略配置
     * @param strategyId 策略ID
     * @return 装配结果，true表示成功，false表示失败
     * @throws IllegalArgumentException 当策略ID无效时抛出
     */
    boolean assembleLotteryStrategy(Long strategyId);
}
```

### 🔧 IStrategyArmory 接口

策略装配库接口，包含策略装配的核心逻辑和抽奖方法。

```java
package com.bhuang.domain.strategy.service.armory;

/**
 * 策略装配库接口
 * 策略装配库(兵工厂)，负责初始化策略计算
 */
public interface IStrategyArmory {
    
    /**
     * 装配抽奖策略配置
     * @param strategyId 策略ID
     * @return 装配结果
     */
    boolean assembleLotteryStrategy(Long strategyId);
    
    /**
     * 获取随机奖品ID - 普通抽奖
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    String getRandomAwardId(Long strategyId);
    
    /**
     * 获取随机奖品ID - 权重抽奖
     * @param strategyId 策略ID  
     * @param ruleWeightValue 权重规则值
     * @return 奖品ID
     */
    String getRandomAwardId(Long strategyId, String ruleWeightValue);
    
    /**
     * 获取策略奖品ID
     * @param strategyId 策略ID
     * @param awardId 奖品ID
     * @return 策略奖品ID
     */
    String getStrategyAwardAssemble(String strategyId, String awardId);
}
```

#### 🔍 方法详解

##### 1. `assembleLotteryStrategy(Long strategyId)`

**功能**: 装配指定策略的抽奖配置

**装配流程**:
1. 查询策略奖品列表
2. 计算概率范围
3. 生成奖品查找表
4. 存储到Redis缓存
5. 处理权重规则（如果存在）

**示例**:
```java
@Test
public void testAssembleLotteryStrategy() {
    boolean result = strategyArmory.assembleLotteryStrategy(100001L);
    assertTrue("策略装配应该成功", result);
}
```

##### 2. `getRandomAwardId(Long strategyId)`

**功能**: 普通抽奖，获取随机奖品ID

**抽奖算法**:
1. 获取策略概率范围 (如: 1000)
2. 生成 1 到 range 的随机数
3. 根据随机数查找对应奖品ID

**示例**:
```java
@Test  
public void testGetRandomAwardId() {
    String awardId = strategyArmory.getRandomAwardId(100001L);
    assertNotNull("奖品ID不应为空", awardId);
    log.info("抽奖结果: {}", awardId);
}
```

##### 3. `getRandomAwardId(Long strategyId, String ruleWeightValue)`

**功能**: 权重抽奖，根据权重值获取随机奖品ID

**权重机制**:
- 根据权重值过滤可用奖品
- 使用权重概率进行抽奖
- 失败时自动回退到普通抽奖

**示例**:
```java
@Test
public void testGetRandomAwardIdByWeight() {
    String awardId = strategyArmory.getRandomAwardId(100001L, "4000");
    assertNotNull("权重抽奖结果不应为空", awardId);
    log.info("权重抽奖结果: {}", awardId);
}
```

---

## 🎲 抽奖调度API

### 📋 IStrategyDispatch 接口

策略调度服务接口，提供抽奖的统一调度入口。

```java
package com.bhuang.domain.strategy.service;

/**
 * 抽奖策略调度服务接口
 * 提供统一的抽奖调度功能
 */
public interface IStrategyDispatch {
    
    /**
     * 获取抽奖策略装配的随机结果
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    String getRandomAwardId(Long strategyId);
    
    /**
     * 获取抽奖策略装配的随机结果 - 权重抽奖
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值  
     * @return 奖品ID
     */
    String getRandomAwardId(Long strategyId, String ruleWeightValue);
}
```

#### 🎯 使用场景

**普通抽奖调度**:
```java
@Service
public class LotteryService {
    
    @Resource
    private IStrategyDispatch strategyDispatch;
    
    public String doLottery(Long strategyId) {
        return strategyDispatch.getRandomAwardId(strategyId);
    }
}
```

**权重抽奖调度**:
```java
public String doWeightLottery(Long strategyId, Integer userScore) {
    String weightValue = determineWeightValue(userScore);
    return strategyDispatch.getRandomAwardId(strategyId, weightValue);
}

private String determineWeightValue(Integer userScore) {
    if (userScore >= 6000) return "6000";
    if (userScore >= 4000) return "4000";
    return null; // 普通抽奖
}
```

---

## 💾 数据仓储API

### 📋 IStrategyRepository 接口

数据仓储抽象接口，定义了所有数据访问操作。

```java
package com.bhuang.domain.strategy.repository;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;

import java.util.List;

/**
 * 策略仓储接口
 * 定义策略相关的数据访问操作
 */
public interface IStrategyRepository {
    
    // ======================== 查询操作 ========================
    
    /**
     * 查询策略奖品列表
     * @param strategyId 策略ID
     * @return 策略奖品实体列表
     */
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);
    
    /**
     * 查询策略规则值
     * @param strategyId 策略ID
     * @param ruleModel 规则模型
     * @return 策略规则实体
     */
    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);
    
    /**
     * 获取概率值范围
     * @param strategyId 策略ID  
     * @return 概率范围
     */
    Integer getRateRange(Long strategyId);
    
    /**
     * 根据权重获取概率值范围
     * @param strategyId 策略ID
     * @param ruleWeightValue 权重规则值
     * @return 权重概率范围
     */
    Integer getRateRangeByWeight(Long strategyId, String ruleWeightValue);
    
    // ======================== 抽奖操作 ========================
    
    /**
     * 根据策略ID和概率值获取奖品信息
     * @param key 组合键
     * @param rateKey 概率键
     * @return 奖品ID
     */
    String getStrategyAwardAssemble(String key, String rateKey);
    
    /**
     * 根据策略ID和概率值获取权重奖品信息
     * @param key 组合键
     * @param rateKey 概率键
     * @return 权重奖品ID
     */
    String getStrategyAwardAssembleByWeight(String key, String rateKey);
    
    // ======================== 存储操作 ========================
    
    /**
     * 存储策略奖品查找表
     * @param key 存储键
     * @param rateRange 概率范围
     * @param shuffleStrategyAwardSearchRateTable 打乱的策略奖品查找表
     */
    void storeStrategyAwardSearchRateTable(String key, Integer rateRange, 
                                         Map<Integer, String> shuffleStrategyAwardSearchRateTable);
    
    /**
     * 存储策略奖品权重查找表
     * @param key 存储键
     * @param rateRange 概率范围  
     * @param shuffleStrategyAwardSearchRateTable 权重查找表
     */
    void storeStrategyAwardSearchRateTableByWeight(String key, Integer rateRange,
                                                 Map<Integer, String> shuffleStrategyAwardSearchRateTable);
}
```

#### 🔍 关键方法说明

##### 1. 查询方法

**queryStrategyAwardList**: 获取策略下所有奖品配置
- 返回包含奖品ID、奖品概率等信息的实体列表
- 支持Redis缓存，提高查询性能

**queryStrategyRule**: 获取策略规则配置
- 主要用于查询权重规则: `rule_weight`
- 返回规则配置字符串，如: `"4000:102,103 6000:102,103,104,105,106,107,108,109"`

##### 2. 概率查询方法

**getRateRange**: 获取普通抽奖的概率范围
- 基于最小概率值计算总概率空间
- 如最小概率0.01，总概率1，则范围为100

**getRateRangeByWeight**: 获取权重抽奖的概率范围
- 根据权重值筛选奖品后重新计算概率范围
- 权重策略不存在时返回0

##### 3. 存储方法

**storeStrategyAwardSearchRateTable**: 存储普通抽奖查找表
```java
// 示例数据结构
Map<Integer, String> rateTable = {
    1: "101",   // 概率位置1对应奖品101
    2: "101",   // 概率位置2对应奖品101
    3: "102",   // 概率位置3对应奖品102
    ...
    100: "109"  // 概率位置100对应奖品109
}
```

---

## 🔴 Redis缓存API

### 📋 IRedisService 接口

Redis缓存服务接口，提供缓存操作的统一抽象。

```java
package com.bhuang.infrastructure.persistent.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis服务接口
 * 提供Redis缓存操作的统一抽象
 */
public interface IRedisService {
    
    // ======================== 基础操作 ========================
    
    /**
     * 设置键值对
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);
    
    /**
     * 设置键值对并指定过期时间
     * @param key 键  
     * @param value 值
     * @param duration 过期时间
     */
    void set(String key, Object value, Duration duration);
    
    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    <T> T get(String key);
    
    /**
     * 删除键
     * @param key 键
     * @return 是否删除成功
     */
    boolean del(String key);
    
    /**
     * 检查键是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);
    
    // ======================== 批量操作 ========================
    
    /**
     * 批量获取
     * @param keys 键列表
     * @return 值映射
     */
    <T> Map<String, T> multiGet(List<String> keys);
    
    /**
     * 批量设置
     * @param keyValueMap 键值映射
     */
    void multiSet(Map<String, Object> keyValueMap);
    
    /**
     * 批量删除
     * @param keys 键列表
     * @return 删除数量
     */
    long del(List<String> keys);
    
    // ======================== Hash操作 ========================
    
    /**
     * Hash设置
     * @param key 主键
     * @param field 字段
     * @param value 值
     */
    void hSet(String key, String field, Object value);
    
    /**
     * Hash获取
     * @param key 主键
     * @param field 字段
     * @return 值
     */
    <T> T hGet(String key, String field);
    
    /**
     * 获取Hash所有字段
     * @param key 主键
     * @return 字段值映射
     */
    <T> Map<String, T> hGetAll(String key);
    
    // ======================== Set操作 ========================
    
    /**
     * Set添加
     * @param key 键
     * @param values 值列表
     * @return 添加数量
     */
    long sAdd(String key, Object... values);
    
    /**
     * 获取Set所有成员
     * @param key 键
     * @return 成员集合
     */
    <T> Set<T> sMembers(String key);
    
    /**
     * 检查Set成员是否存在
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    boolean sIsMember(String key, Object value);
}
```

#### 🗝️ 缓存Key设计规范

BigMark系统使用统一的Redis Key命名规范：

```java
public class Constants {
    
    /**
     * 策略奖品列表Key
     * 格式: strategy#{strategyId}#awardlist
     */
    public static String STRATEGY_AWARD_LIST_KEY = "strategy#{strategyId}#awardlist";
    
    /**
     * 策略概率范围Key  
     * 格式: strategy#{strategyId}#raterange
     */
    public static String STRATEGY_RATE_RANGE_KEY = "strategy#{strategyId}#raterange";
    
    /**
     * 策略规则Key
     * 格式: strategy#{strategyId}#rule#{ruleName}
     */
    public static String STRATEGY_RULE_KEY = "strategy#{strategyId}#rule#{ruleName}";
    
    /**
     * 权重概率范围Key
     * 格式: strategy#{strategyId}#raterange#weight#{weightValue}
     */
    public static String STRATEGY_RATE_RANGE_WEIGHT_KEY = "strategy#{strategyId}#raterange#weight#{weightValue}";
    
    /**
     * 概率查找表Key
     * 格式: strategy#{strategyId}#assemble#{rate}
     */
    public static String STRATEGY_RATE_TABLE_KEY = "strategy#{strategyId}#assemble#{rate}";
    
    /**
     * 权重查找表Key  
     * 格式: strategy#{strategyId}#assemble#weight#{weightValue}#{rate}
     */
    public static String STRATEGY_RATE_TABLE_WEIGHT_KEY = "strategy#{strategyId}#assemble#weight#{weightValue}#{rate}";
}
```

#### 💾 缓存使用示例

```java
@Service
public class StrategyRepositoryImpl implements IStrategyRepository {
    
    @Resource
    private IRedisService redisService;
    
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        String cacheKey = Constants.STRATEGY_AWARD_LIST_KEY
                         .replace("{strategyId}", String.valueOf(strategyId));
        
        // 先查缓存
        List<StrategyAwardEntity> cacheResult = redisService.get(cacheKey);
        if (null != cacheResult) {
            return cacheResult;
        }
        
        // 查数据库
        List<StrategyAwardEntity> result = queryFromDatabase(strategyId);
        
        // 存入缓存，24小时过期
        redisService.set(cacheKey, result, Duration.ofHours(24));
        
        return result;
    }
}
```

---

## 📊 实体类说明

### 🎁 StrategyAwardEntity - 策略奖品实体

```java
package com.bhuang.domain.strategy.model.entity;

import java.math.BigDecimal;

/**
 * 策略奖品实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {
    
    /** 抽奖策略ID */
    private Long strategyId;
    
    /** 抽奖奖品ID - 内部流转使用 */  
    private String awardId;
    
    /** 抽奖奖品标题 */
    private String awardTitle;
    
    /** 抽奖奖品副标题 */
    private String awardSubtitle;
    
    /** 奖品库存数量 */
    private Integer awardCount;
    
    /** 奖品库存剩余 */
    private Integer awardCountSurplus;
    
    /** 奖品中奖概率 */
    private BigDecimal awardRate;
    
    /** 规则模型，rule配置的模型同步到此表，便于使用 */
    private String ruleModels;
    
    /** 排序 */
    private Integer sort;
}
```

#### 🔍 字段说明

- **strategyId**: 策略ID，关联具体的抽奖策略
- **awardId**: 奖品ID，系统内部使用的奖品标识
- **awardRate**: 奖品概率，使用BigDecimal确保精度，如0.01表示1%
- **ruleModels**: 规则模型，如"rule_random,rule_luck_award"

### 📏 StrategyRuleEntity - 策略规则实体

```java
package com.bhuang.domain.strategy.model.entity;

/**
 * 策略规则实体
 */
@Data
@Builder  
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {
    
    /** 抽奖策略ID */
    private Long strategyId;
    
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private String awardId;
    
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    
    /** 抽奖规则比值 */
    private String ruleValue;
    
    /** 抽奖规则描述 */
    private String ruleDesc;
}
```

#### 🎯 权重规则格式

权重规则使用特定格式存储在`ruleValue`字段中：

```java
// 权重规则示例
"4000:102,103 6000:102,103,104,105,106,107,108,109"

// 解析逻辑
String[] ruleValueGroups = ruleValue.split(" ");
for (String ruleValueGroup : ruleValueGroups) {
    String[] parts = ruleValueGroup.split(":");
    String weightValue = parts[0];        // "4000"
    String[] awardIds = parts[1].split(","); // ["102", "103"]
    
    ruleWeightValues.put(weightValue, awardIds);
}
```

---

## 🧪 使用示例

### 🚀 完整使用流程

```java
@SpringBootTest
@Slf4j
public class StrategyUsageExample {
    
    @Resource
    private IStrategyAssemble strategyAssemble;
    
    @Resource  
    private IStrategyDispatch strategyDispatch;
    
    @Test
    public void completeUsageExample() {
        Long strategyId = 100001L;
        
        // 1. 装配策略
        log.info("开始装配策略: {}", strategyId);
        boolean assembleResult = strategyAssemble.assembleLotteryStrategy(strategyId);
        assertTrue("策略装配失败", assembleResult);
        log.info("策略装配完成");
        
        // 2. 普通抽奖测试
        log.info("开始普通抽奖测试");
        Map<String, Integer> awardCount = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String awardId = strategyDispatch.getRandomAwardId(strategyId);
            awardCount.merge(awardId, 1, Integer::sum);
        }
        
        log.info("普通抽奖结果统计:");
        awardCount.forEach((awardId, count) -> 
            log.info("奖品 {} 中奖 {} 次，概率: {:.2f}%", 
                    awardId, count, count * 100.0 / 1000));
        
        // 3. 权重抽奖测试  
        log.info("开始权重抽奖测试");
        Map<String, Integer> weightAwardCount = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String awardId = strategyDispatch.getRandomAwardId(strategyId, "4000");
            weightAwardCount.merge(awardId, 1, Integer::sum);
        }
        
        log.info("权重抽奖结果统计:");
        weightAwardCount.forEach((awardId, count) -> 
            log.info("奖品 {} 中奖 {} 次，概率: {:.2f}%", 
                    awardId, count, count * 100.0 / 1000));
    }
}
```

### 🎯 策略配置示例

#### 数据库配置

```sql
-- 策略表
INSERT INTO strategy (id, strategy_id, strategy_desc, strategy_rule) 
VALUES (1, 100001, '阶段抽奖策略', 'rule_weight');

-- 策略奖品表
INSERT INTO strategy_award (id, strategy_id, award_id, award_title, award_subtitle, award_count, award_count_surplus, award_rate, sort) 
VALUES 
(1, 100001, '101', '随机积分', '1积分', 80000, 80000, 0.8, 1),
(2, 100001, '102', '随机积分', '10积分', 10000, 10000, 0.1, 2),
(3, 100001, '103', '随机积分', '50积分', 5000, 5000, 0.05, 3),
(4, 100001, '104', '随机积分', '100积分', 4000, 4000, 0.04, 4),
(5, 100001, '105', '随机积分', '500积分', 800, 800, 0.008, 5),
(6, 100001, '106', 'OpenAI会员卡', '增加10次对话', 100, 100, 0.001, 6),
(7, 100001, '107', 'OpenAI会员卡', '增加100次对话', 50, 50, 0.0005, 7),
(8, 100001, '108', 'OpenAI会员卡', '增加1000次对话', 20, 20, 0.0002, 8),
(9, 100001, '109', '苹果手机', 'iPhone 15 Pro', 1, 1, 0.0001, 9);

-- 策略规则表
INSERT INTO strategy_rule (id, strategy_id, award_id, rule_type, rule_model, rule_value, rule_desc) 
VALUES (1, 100001, '', 1, 'rule_weight', '4000:102,103 6000:102,103,104,105,106,107,108,109', '根据积分选择奖品');
```

#### 权重规则说明

- **4000分以上**: 可抽取奖品 102, 103
- **6000分以上**: 可抽取奖品 102, 103, 104, 105, 106, 107, 108, 109
- **低于4000分**: 只能参与普通抽奖

### 🔧 自定义扩展示例

#### 添加新的规则类型

```java
// 1. 定义新的规则模型
public static final String RULE_BLACKLIST = "rule_blacklist";

// 2. 在规则表中添加配置
INSERT INTO strategy_rule (strategy_id, rule_model, rule_value, rule_desc) 
VALUES (100001, 'rule_blacklist', '101,102', '黑名单奖品，禁止获得');

// 3. 在代码中处理新规则
if (ruleModels.contains(RULE_BLACKLIST)) {
    String[] blacklistAwards = ruleValue.split(",");
    // 过滤黑名单奖品的逻辑
}
```

### 📊 性能测试示例

```java
@Test
public void performanceTest() {
    Long strategyId = 100001L;
    
    // 装配策略
    strategyAssemble.assembleLotteryStrategy(strategyId);
    
    // 性能测试
    int testCount = 100000;
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < testCount; i++) {
        strategyDispatch.getRandomAwardId(strategyId);
    }
    
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    
    log.info("性能测试结果:");
    log.info("总抽奖次数: {}", testCount);
    log.info("总耗时: {} ms", totalTime);
    log.info("平均耗时: {:.4f} ms/次", (double) totalTime / testCount);
    log.info("QPS: {:.0f} 次/秒", testCount * 1000.0 / totalTime);
}
```

**预期性能指标**:
- **QPS**: > 10,000 次/秒
- **平均响应时间**: < 0.1ms
- **内存使用**: 稳定，无内存泄漏

---

## 📈 最佳实践

### 1. **策略装配最佳实践**
- 在系统启动时预装配热门策略
- 定期检查策略配置的有效性
- 使用缓存预热避免冷启动

### 2. **抽奖调用最佳实践**  
- 使用连接池避免频繁创建连接
- 实现降级策略，缓存不可用时从数据库查询
- 添加监控和告警机制

### 3. **缓存使用最佳实践**
- 设置合理的缓存过期时间
- 使用批量操作提高性能
- 实现缓存更新策略

### 4. **错误处理最佳实践**
- 实现完整的异常处理机制
- 提供详细的错误日志
- 添加重试机制

---

<div align="center">

**📖 更多API文档请访问 [GitHub Wiki](https://github.com/Hwangsome/Bhuang-BigMark/wiki)**

Made with ❤️ by BigMark Team

</div> 