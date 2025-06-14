package com.bhuang.infrastructure.persistent.constants;

import java.math.BigDecimal;

/**
 * 常量类 - 统一管理所有系统常量
 * @author bhuang
 */
public class Constants {

    /**
     * Redis缓存相关常量
     */
    public static final class Redis {
        
        /**
         * 策略奖品列表缓存Key前缀
         */
        public static final String STRATEGY_AWARD_LIST_PREFIX = "strategy:award:list:";

        /**
         * 策略概率范围缓存Key前缀
         */
        public static final String STRATEGY_RATE_RANGE_PREFIX = "strategy:rate:range:";

        /**
         * 策略奖品装配缓存Key前缀
         */
        public static final String STRATEGY_AWARD_ASSEMBLE_PREFIX = "strategy:award:assemble:";

        /**
         * 缓存过期时间（秒）
         */
        public static final class ExpireTime {
            /** 1小时 */
            public static final long ONE_HOUR = 3600L;
            /** 1天 */
            public static final long ONE_DAY = 86400L;
            /** 1周 */
            public static final long ONE_WEEK = 604800L;
        }

        /**
         * 生成策略奖品列表缓存Key
         * @param strategyId 策略ID
         * @return 缓存Key
         */
        public static String getStrategyAwardListKey(Long strategyId) {
            return STRATEGY_AWARD_LIST_PREFIX + strategyId;
        }

        /**
         * 生成策略概率范围缓存Key
         * @param strategyId 策略ID
         * @return 缓存Key
         */
        public static String getStrategyRateRangeKey(Long strategyId) {
            return STRATEGY_RATE_RANGE_PREFIX + strategyId;
        }

        /**
         * 生成策略奖品装配缓存Key
         * @param strategyId 策略ID
         * @param rateKey 概率key
         * @return 缓存Key
         */
        public static String getStrategyAwardAssembleKey(Long strategyId, Integer rateKey) {
            return STRATEGY_AWARD_ASSEMBLE_PREFIX + strategyId + "_" + rateKey;
        }
    }

    /**
     * 业务相关常量
     */
    public static final class Business {
        
        /**
         * 默认策略ID
         */
        public static final Long DEFAULT_STRATEGY_ID = 100001L;
        
        /**
         * 最大奖品数量
         */
        public static final Integer MAX_AWARD_COUNT = 1000;
        
        /**
         * 默认概率精度
         */
        public static final Integer DEFAULT_RATE_PRECISION = 100;
        
        /**
         * 概率范围计算常量
         */
        public static final class ProbabilityRange {
            /** 概率计算精度 */
            public static final BigDecimal PRECISION = new BigDecimal("0.0001");
            /** 百分位 */
            public static final BigDecimal PERCENTAGE = new BigDecimal("0.01");
            /** 千分位 */
            public static final BigDecimal PERMILLAGE = new BigDecimal("0.001");
            /** 万分位 */
            public static final BigDecimal TEN_THOUSANDTH = new BigDecimal("0.0001");
        }
    }

    /**
     * 系统配置常量
     */
    public static final class System {
        
        /**
         * 默认字符编码
         */
        public static final String DEFAULT_CHARSET = "UTF-8";
        
        /**
         * 分页默认大小
         */
        public static final Integer DEFAULT_PAGE_SIZE = 20;
        
        /**
         * 最大分页大小
         */
        public static final Integer MAX_PAGE_SIZE = 100;
    }

    /**
     * 状态码常量
     */
    public static final class Status {
        
        /**
         * 成功
         */
        public static final String SUCCESS = "0000";
        
        /**
         * 失败
         */
        public static final String FAIL = "0001";
        
        /**
         * 参数错误
         */
        public static final String PARAM_ERROR = "0002";
        
        /**
         * 系统错误
         */
        public static final String SYSTEM_ERROR = "0003";
    }
} 