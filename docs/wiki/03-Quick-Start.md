# 🚀 快速开始指南

> 本指南将帮助您快速搭建和运行BigMark抽奖策略系统

## 📋 目录

1. [⚡ 系统要求](#-系统要求)
2. [📦 环境准备](#-环境准备)
3. [🔧 项目配置](#-项目配置)
4. [🚀 启动应用](#-启动应用)
5. [🧪 运行测试](#-运行测试)
6. [💡 使用示例](#-使用示例)
7. [❓ 常见问题](#-常见问题)

---

## ⚡ 系统要求

### 📋 硬件要求
- **CPU**: 2核心以上
- **内存**: 4GB以上
- **磁盘**: 10GB可用空间
- **网络**: 稳定的网络连接

### 💻 软件环境要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| **Java** | JDK 17+ | 推荐使用 OpenJDK 17 |
| **Maven** | 3.8.0+ | 项目构建工具 |
| **MySQL** | 8.0+ | 数据库服务 |
| **Redis** | 7.0+ | 缓存服务 |
| **Git** | 2.0+ | 版本控制工具 |

### 🔍 环境检查

运行以下命令检查您的环境：

```bash
# 检查Java版本
java -version

# 检查Maven版本  
mvn -version

# 检查MySQL服务
mysql --version

# 检查Redis服务
redis-cli --version
```

---

## 📦 环境准备

### 1. 🗄️ MySQL数据库配置

#### 安装MySQL (如果未安装)

**macOS**:
```bash
# 使用Homebrew安装
brew install mysql

# 启动MySQL服务
brew services start mysql
```

**Ubuntu/Debian**:
```bash
# 安装MySQL
sudo apt update
sudo apt install mysql-server

# 启动MySQL服务
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Windows**:
- 下载并安装 [MySQL安装程序](https://dev.mysql.com/downloads/installer/)

#### 创建数据库和用户

```sql
-- 1. 登录MySQL
mysql -u root -p

-- 2. 创建数据库
CREATE DATABASE big_market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 创建用户(可选，也可以使用root)
CREATE USER 'bigmark'@'%' IDENTIFIED BY 'bigmark123';
GRANT ALL PRIVILEGES ON big_market.* TO 'bigmark'@'%';
FLUSH PRIVILEGES;

-- 4. 验证创建
SHOW DATABASES;
SELECT user, host FROM mysql.user WHERE user = 'bigmark';
```

#### 导入数据库结构

```bash
# 进入项目目录
cd Bhuang-BigMark

# 导入数据库结构(假设有SQL文件)
mysql -u root -p big_market < docs/sql/schema.sql

# 导入测试数据
mysql -u root -p big_market < docs/sql/test_data.sql
```

### 2. 🔴 Redis缓存配置

#### 安装Redis (如果未安装)

**macOS**:
```bash
# 使用Homebrew安装
brew install redis

# 启动Redis服务
brew services start redis
```

**Ubuntu/Debian**:
```bash
# 安装Redis
sudo apt update
sudo apt install redis-server

# 启动Redis服务
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

**Windows**:
- 下载并安装 [Redis for Windows](https://github.com/tporadowski/redis/releases)

#### 验证Redis服务

```bash
# 测试Redis连接
redis-cli ping

# 应该返回: PONG

# 查看Redis信息
redis-cli info server
```

### 3. ☕ Java开发环境

#### 安装JDK 17

**使用SDKMAN (推荐)**:
```bash
# 安装SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装JDK 17
sdk install java 17.0.8-tem
sdk use java 17.0.8-tem

# 验证安装
java -version
```

**直接下载安装**:
- 访问 [OpenJDK下载页面](https://openjdk.org/install/)
- 下载对应平台的JDK 17
- 按照安装向导完成安装

### 4. 🛠️ Maven构建工具

#### 安装Maven

**macOS**:
```bash
brew install maven
```

**Ubuntu/Debian**:
```bash
sudo apt update
sudo apt install maven
```

**Windows**:
- 下载 [Maven](https://maven.apache.org/download.cgi)
- 解压并配置环境变量

#### 配置Maven

创建或编辑 `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <!-- 配置阿里云镜像，加速下载 -->
    <mirrors>
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>*</mirrorOf>
            <name>阿里云公共仓库</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
    
    <!-- 配置JDK版本 -->
    <profiles>
        <profile>
            <id>jdk17</id>
            <activation>
                <activeByDefault>true</activeByDefault>
                <jdk>17</jdk>
            </activation>
            <properties>
                <maven.compiler.source>17</maven.compiler.source>
                <maven.compiler.target>17</maven.compiler.target>
                <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
            </properties>
        </profile>
    </profiles>
</settings>
```

---

## 🔧 项目配置

### 1. 📥 获取项目代码

```bash
# 克隆项目
git clone https://github.com/Hwangsome/Bhuang-BigMark.git

# 进入项目目录
cd Bhuang-BigMark

# 查看项目结构
tree -L 2
```

### 2. ⚙️ 配置文件设置

#### 主配置文件: `application-dev.yml`

编辑 `Bhuang-BigMark-app/src/main/resources/application-dev.yml`:

```yaml
server:
  port: 8091

# 数据库配置
spring:
  datasource:
    username: root          # 修改为您的数据库用户名
    password: root          # 修改为您的数据库密码
    url: jdbc:mysql://127.0.0.1:3306/big_market?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=false
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # HikariCP连接池配置
  hikari:
    pool-name: Retail_HikariCP
    minimum-idle: 15
    idle-timeout: 180000
    maximum-pool-size: 25
    auto-commit: true
    max-lifetime: 1800000
    connection-timeout: 30000
    connection-test-query: SELECT 1

  # Redis配置
  redis:
    host: 127.0.0.1        # Redis主机地址
    port: 6379             # Redis端口
    timeout: 10000ms       # 连接超时时间
    database: 0            # 使用的数据库索引
    # password: yourpassword  # 如果Redis设置了密码，请取消注释并设置

# MyBatis配置
mybatis:
  mapper-locations: classpath:/mybatis/mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

# 日志配置
logging:
  level:
    com.bhuang: DEBUG
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 线程池配置
thread:
  pool:
    executor:
      config:
        core-pool-size: 20
        max-pool-size: 50
        keep-alive-time: 5000
        block-queue-size: 5000
        policy: CallerRunsPolicy
```

#### 测试配置文件: `application-test.yml`

创建 `Bhuang-BigMark-app/src/test/resources/application-test.yml`:

```yaml
# 测试环境配置
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/big_market_test?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=false
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: 127.0.0.1
    port: 6379
    timeout: 10000ms
    database: 1  # 使用不同的数据库避免与开发环境冲突

# 测试日志级别
logging:
  level:
    com.bhuang: DEBUG
    org.springframework: WARN
    org.mybatis: DEBUG
```

### 3. 🗄️ 数据库初始化

#### 创建数据库表结构

创建 `docs/sql/schema.sql`:

```sql
-- 策略表
CREATE TABLE strategy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    strategy_id BIGINT NOT NULL COMMENT '抽奖策略ID',
    strategy_desc VARCHAR(128) NOT NULL COMMENT '抽奖策略描述',
    strategy_rule VARCHAR(256) DEFAULT NULL COMMENT '抽奖规则',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_strategy_id (strategy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略表';

-- 策略奖品表
CREATE TABLE strategy_award (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    strategy_id BIGINT NOT NULL COMMENT '抽奖策略ID',
    award_id VARCHAR(32) NOT NULL COMMENT '抽奖奖品ID',
    award_title VARCHAR(128) NOT NULL COMMENT '抽奖奖品标题',
    award_subtitle VARCHAR(128) DEFAULT NULL COMMENT '抽奖奖品副标题',
    award_count INT NOT NULL DEFAULT 0 COMMENT '奖品数量',
    award_count_surplus INT NOT NULL DEFAULT 0 COMMENT '奖品数量剩余',
    award_rate DECIMAL(6,4) NOT NULL COMMENT '奖品中奖概率',
    rule_models VARCHAR(256) DEFAULT NULL COMMENT '规则模型',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_strategy_id (strategy_id),
    KEY idx_award_id (award_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略奖品表';

-- 策略规则表
CREATE TABLE strategy_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    strategy_id BIGINT NOT NULL COMMENT '抽奖策略ID',
    award_id VARCHAR(32) DEFAULT NULL COMMENT '抽奖奖品ID',
    rule_type TINYINT NOT NULL COMMENT '抽象规则类型；1-策略规则、2-奖品规则',
    rule_model VARCHAR(32) NOT NULL COMMENT '抽奖规则类型',
    rule_value VARCHAR(256) NOT NULL COMMENT '抽奖规则比值',
    rule_desc VARCHAR(128) NOT NULL COMMENT '抽奖规则描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_strategy_id (strategy_id),
    KEY idx_rule_model (rule_model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略规则表';

-- 奖品表
CREATE TABLE award (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    award_id VARCHAR(32) NOT NULL COMMENT '抽奖奖品ID',
    award_key VARCHAR(32) NOT NULL COMMENT '奖品对接标识',
    award_config VARCHAR(32) NOT NULL COMMENT '奖品配置信息',
    award_desc VARCHAR(128) NOT NULL COMMENT '奖品内容描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_award_id (award_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品表';
```

#### 插入测试数据

创建 `docs/sql/test_data.sql`:

```sql
-- 插入策略数据
INSERT INTO strategy (strategy_id, strategy_desc, strategy_rule) VALUES
(100001, '阶段抽奖策略', 'rule_weight');

-- 插入策略奖品数据
INSERT INTO strategy_award (strategy_id, award_id, award_title, award_subtitle, award_count, award_count_surplus, award_rate, sort) VALUES
(100001, '101', '随机积分', '1积分', 80000, 80000, 0.8000, 1),
(100001, '102', '随机积分', '10积分', 10000, 10000, 0.1000, 2),
(100001, '103', '随机积分', '50积分', 5000, 5000, 0.0500, 3),
(100001, '104', '随机积分', '100积分', 4000, 4000, 0.0400, 4),
(100001, '105', '随机积分', '500积分', 800, 800, 0.0080, 5),
(100001, '106', 'OpenAI会员卡', '增加10次对话', 100, 100, 0.0010, 6),
(100001, '107', 'OpenAI会员卡', '增加100次对话', 50, 50, 0.0005, 7),
(100001, '108', 'OpenAI会员卡', '增加1000次对话', 20, 20, 0.0002, 8),
(100001, '109', '苹果手机', 'iPhone 15 Pro', 1, 1, 0.0001, 9);

-- 插入策略规则数据
INSERT INTO strategy_rule (strategy_id, award_id, rule_type, rule_model, rule_value, rule_desc) VALUES
(100001, NULL, 1, 'rule_weight', '4000:102,103 6000:102,103,104,105,106,107,108,109', '根据积分选择奖品');

-- 插入奖品数据
INSERT INTO award (award_id, award_key, award_config, award_desc) VALUES
('101', 'user_credit_random', '1', '随机积分1个'),
('102', 'user_credit_random', '10', '随机积分10个'),
('103', 'user_credit_random', '50', '随机积分50个'),
('104', 'user_credit_random', '100', '随机积分100个'),
('105', 'user_credit_random', '500', '随机积分500个'),
('106', 'openai_use_count', '10', 'OpenAI使用次数10次'),
('107', 'openai_use_count', '100', 'OpenAI使用次数100次'),
('108', 'openai_use_count', '1000', 'OpenAI使用次数1000次'),
('109', 'user_phone', 'iphone15', '苹果手机iPhone 15 Pro');
```

#### 执行数据库初始化

```bash
# 导入表结构
mysql -u root -p big_market < docs/sql/schema.sql

# 导入测试数据
mysql -u root -p big_market < docs/sql/test_data.sql

# 验证数据导入
mysql -u root -p big_market -e "
SELECT COUNT(*) as strategy_count FROM strategy;
SELECT COUNT(*) as award_count FROM strategy_award;
SELECT COUNT(*) as rule_count FROM strategy_rule;
"
```

---

## 🚀 启动应用

### 1. 📦 构建项目

```bash
# 进入项目根目录
cd Bhuang-BigMark

# 清理并编译项目
mvn clean compile

# 打包项目(跳过测试)
mvn clean package -DskipTests

# 完整构建(包含测试)
mvn clean install
```

### 2. 🔧 解决可能的构建问题

#### 常见问题及解决方案

**问题1: 依赖下载失败**
```bash
# 清理Maven缓存并重新下载
mvn dependency:purge-local-repository
mvn clean install
```

**问题2: 编译版本不匹配**
```bash
# 确认Java版本
java -version
mvn -version

# 如果版本不对，重新设置JAVA_HOME
export JAVA_HOME=/path/to/jdk17
```

**问题3: 数据库连接失败**
```bash
# 检查MySQL服务状态
systemctl status mysql  # Linux
brew services list | grep mysql  # macOS

# 测试数据库连接
mysql -u root -p -e "SELECT 1"
```

### 3. 🏃 运行应用

#### 方式1: 使用Maven运行

```bash
# 在app模块中运行
cd Bhuang-BigMark-app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 方式2: 使用IDE运行

1. 导入项目到IDE (IntelliJ IDEA 推荐)
2. 配置JDK 17
3. 运行 `Application.java` 主类
4. 指定运行配置: `-Dspring.profiles.active=dev`

#### 方式3: 使用JAR包运行

```bash
# 进入app模块target目录
cd Bhuang-BigMark-app/target

# 运行JAR包
java -jar -Dspring.profiles.active=dev Bhuang-BigMark-app-1.0.jar
```

### 4. ✅ 验证启动

#### 检查应用启动日志

应该看到类似以下日志：

```
  ____  _       __  __            _    
 | __ )(_) __ _|  \/  | __ _ _ __| | __
 |  _ \| |/ _` | |\/| |/ _` | '__| |/ /
 | |_) | | (_| | |  | | (_| | |  |   < 
 |____/|_|\__, |_|  |_|\__,_|_|  |_|\_\
          |___/                        

2024-01-20 10:30:45 [main] INFO  c.b.Application - Started Application in 3.45 seconds
2024-01-20 10:30:45 [main] INFO  c.b.Application - 
=======================================================
🎯 BigMark 抽奖策略系统启动成功! 
🚀 访问地址: http://localhost:8091
📝 API文档: http://localhost:8091/swagger-ui.html
=======================================================
```

#### 检查端口监听

```bash
# 检查8091端口是否被监听
netstat -tlnp | grep 8091
# 或者
lsof -i :8091
```

#### 检查健康状态

```bash
# 如果有健康检查端点
curl http://localhost:8091/actuator/health

# 或者简单的ping测试
curl http://localhost:8091/ping
```

---

## 🧪 运行测试

### 1. 🔧 单元测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl Bhuang-BigMark-domain

# 运行特定测试类
mvn test -Dtest=StrategyArmoryTest

# 运行特定测试方法
mvn test -Dtest=StrategyArmoryTest#testAssembleLotteryStrategy
```

### 2. 🎯 集成测试

```bash
# 运行集成测试
mvn verify

# 运行策略装配测试
mvn test -Dtest=StrategyArmoryTest -pl Bhuang-BigMark-app

# 查看测试报告
open Bhuang-BigMark-app/target/surefire-reports/index.html
```

### 3. 📊 测试覆盖率

```bash
# 生成测试覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

### 4. 🚀 性能测试

运行性能测试：

```java
@Test
public void performanceTest() {
    // 装配策略
    boolean result = strategyArmory.assembleLotteryStrategy(100001L);
    assertTrue(result);
    
    // 性能测试
    int testCount = 10000;
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < testCount; i++) {
        String awardId = strategyDispatch.getRandomAwardId(100001L);
        assertNotNull(awardId);
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

---

## 💡 使用示例

### 1. 🎯 基础抽奖流程

```java
@SpringBootTest
@Slf4j
public class QuickStartExample {
    
    @Resource
    private IStrategyAssemble strategyAssemble;
    
    @Resource
    private IStrategyDispatch strategyDispatch;
    
    @Test
    public void quickStart() {
        Long strategyId = 100001L;
        
        // 1. 装配策略
        log.info("🔧 开始装配策略: {}", strategyId);
        boolean success = strategyAssemble.assembleLotteryStrategy(strategyId);
        assertTrue("策略装配失败", success);
        log.info("✅ 策略装配成功");
        
        // 2. 执行抽奖
        log.info("🎲 开始抽奖测试");
        for (int i = 0; i < 5; i++) {
            String awardId = strategyDispatch.getRandomAwardId(strategyId);
            log.info("🎁 第{}次抽奖结果: {}", i + 1, awardId);
        }
    }
}
```

### 2. ⚖️ 权重抽奖示例

```java
@Test
public void weightedLotteryExample() {
    Long strategyId = 100001L;
    
    // 装配策略
    strategyAssemble.assembleLotteryStrategy(strategyId);
    
    // 模拟不同积分用户抽奖
    testUserLottery("普通用户", strategyId, null);
    testUserLottery("VIP用户(4000分)", strategyId, "4000");
    testUserLottery("SVIP用户(6000分)", strategyId, "6000");
}

private void testUserLottery(String userType, Long strategyId, String weightValue) {
    log.info("=== {} 抽奖测试 ===", userType);
    Map<String, Integer> awardCount = new HashMap<>();
    
    for (int i = 0; i < 100; i++) {
        String awardId = weightValue == null 
            ? strategyDispatch.getRandomAwardId(strategyId)
            : strategyDispatch.getRandomAwardId(strategyId, weightValue);
        awardCount.merge(awardId, 1, Integer::sum);
    }
    
    awardCount.forEach((awardId, count) -> 
        log.info("奖品 {} 中奖 {} 次，概率: {:.1f}%", 
                awardId, count, count * 100.0 / 100));
}
```

### 3. 📊 概率验证示例

```java
@Test
public void probabilityValidationExample() {
    Long strategyId = 100001L;
    strategyAssemble.assembleLotteryStrategy(strategyId);
    
    // 大量抽奖验证概率
    int totalTests = 10000;
    Map<String, Integer> awardCount = new HashMap<>();
    
    for (int i = 0; i < totalTests; i++) {
        String awardId = strategyDispatch.getRandomAwardId(strategyId);
        awardCount.merge(awardId, 1, Integer::sum);
    }
    
    log.info("=== 概率验证结果 ({} 次抽奖) ===", totalTests);
    awardCount.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByKey())
        .forEach(entry -> {
            String awardId = entry.getKey();
            int count = entry.getValue();
            double actualRate = count * 100.0 / totalTests;
            log.info("奖品 {}: {} 次 ({:.2f}%)", awardId, count, actualRate);
        });
}
```

---

## ❓ 常见问题

### 🚨 启动问题

#### 问题1: 端口被占用
```
Error: Port 8091 is already in use
```

**解决方案**:
```bash
# 查找占用端口的进程
lsof -i :8091

# 杀死进程
kill -9 PID

# 或者修改配置文件中的端口
server:
  port: 8092
```

#### 问题2: 数据库连接失败
```
Communications link failure
```

**解决方案**:
1. 检查MySQL服务是否启动
2. 验证数据库连接信息
3. 检查网络连接
4. 确认数据库权限

```bash
# 检查MySQL服务
systemctl status mysql

# 测试连接
mysql -h 127.0.0.1 -P 3306 -u root -p

# 检查用户权限
mysql -u root -p -e "SELECT user, host FROM mysql.user;"
```

#### 问题3: Redis连接失败
```
Unable to connect to Redis
```

**解决方案**:
```bash
# 检查Redis服务
redis-cli ping

# 启动Redis服务
brew services start redis  # macOS
sudo systemctl start redis-server  # Ubuntu

# 检查Redis配置
redis-cli config get "*"
```

### 🔧 构建问题

#### 问题1: Maven依赖下载失败
```
Failed to read artifact descriptor
```

**解决方案**:
```bash
# 清理本地仓库
rm -rf ~/.m2/repository

# 重新下载依赖
mvn clean install -U
```

#### 问题2: 编译错误
```
Source option 17 is no longer supported
```

**解决方案**:
1. 确认Java版本: `java -version`
2. 设置JAVA_HOME环境变量
3. 更新IDE的JDK配置

### 🧪 测试问题

#### 问题1: 测试数据库连接失败

**解决方案**:
```bash
# 创建测试数据库
mysql -u root -p -e "CREATE DATABASE big_market_test;"

# 导入测试数据
mysql -u root -p big_market_test < docs/sql/schema.sql
mysql -u root -p big_market_test < docs/sql/test_data.sql
```

#### 问题2: Redis缓存清理

**解决方案**:
```bash
# 清理Redis缓存
redis-cli flushdb

# 或者清理特定key
redis-cli del "strategy#*"
```

### 📊 性能问题

#### 问题1: 抽奖性能较低

**解决方案**:
1. 检查Redis连接池配置
2. 优化数据库连接池
3. 调整JVM参数

```yaml
# 优化Redis配置
spring:
  redis:
    jedis:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5
        max-wait: 3000ms
```

#### 问题2: 内存使用过高

**解决方案**:
```bash
# 添加JVM参数
java -Xms512m -Xmx1g -XX:+UseG1GC -jar app.jar

# 监控内存使用
jstat -gc PID 1s
```

### 🆘 获取帮助

如果遇到其他问题，可以通过以下方式获取帮助：

1. **查看日志**: 检查 `logs/` 目录下的日志文件
2. **GitHub Issues**: 提交问题到项目的Issues页面
3. **文档**: 查阅更详细的[架构文档](01-System-Architecture.md)
4. **API文档**: 参考[API文档](02-API-Documentation.md)

---

## 🎉 恭喜！

您已经成功搭建并运行了BigMark抽奖策略系统！

### 🔥 下一步建议

1. **探索功能**: 尝试不同的抽奖策略和权重配置
2. **学习架构**: 深入了解[系统架构设计](01-System-Architecture.md)
3. **API集成**: 参考[API文档](02-API-Documentation.md)进行集成
4. **性能优化**: 根据实际需求进行性能调优
5. **功能扩展**: 基于业务需求添加新功能

---

<div align="center">

**🚀 开始您的抽奖系统之旅吧！**

**📖 更多文档请访问 [GitHub Wiki](https://github.com/Hwangsome/Bhuang-BigMark/wiki)**

Made with ❤️ by BigMark Team

</div> 