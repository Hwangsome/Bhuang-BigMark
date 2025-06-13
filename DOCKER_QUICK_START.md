# 🚀 Docker 快速开始指南

## 📋 前提条件

- 安装 Docker
- 有阿里云镜像仓库访问权限

## 🎯 快速使用（推荐）

我们提供了一个便捷的管理脚本 `docker-run.sh`：

```bash
# 1. 登录阿里云
./docker-run.sh login

# 2. 拉取最新镜像
./docker-run.sh pull

# 3. 运行开发环境
./docker-run.sh run

# 4. 查看运行状态
./docker-run.sh status

# 5. 查看日志
./docker-run.sh logs

# 6. 停止应用
./docker-run.sh stop
```

## 🔧 手动使用

如果您喜欢手动操作：

### 1️⃣ 登录阿里云 Docker Registry

```bash
docker login --username=黄帅啊 crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com
```

### 2️⃣ 拉取镜像

```bash
# 拉取最新版本
docker pull crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest

# 或拉取指定版本
docker pull crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:v1.0.0
```

### 3️⃣ 运行容器

**开发环境：**
```bash
docker run -d \
  --name bhuang-bigmark-app \
  -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=dev \
  crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest
```

**生产环境：**
```bash
docker run -d \
  --name bhuang-bigmark-app \
  -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JAVA_OPTS='-Xms1g -Xmx2g' \
  --restart unless-stopped \
  crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest
```

### 4️⃣ 验证运行

```bash
# 检查容器状态
docker ps

# 查看应用日志
docker logs -f bhuang-bigmark-app

# 访问应用
curl http://localhost:8091/actuator/health
```

## 🎛️ 管理脚本命令大全

```bash
./docker-run.sh help                    # 显示帮助
./docker-run.sh login                   # 登录阿里云
./docker-run.sh pull [tag]             # 拉取镜像
./docker-run.sh run [tag] [profile]    # 运行容器
./docker-run.sh stop                   # 停止容器
./docker-run.sh restart                # 重启容器
./docker-run.sh logs                   # 查看日志
./docker-run.sh status                 # 查看状态
./docker-run.sh clean                  # 清理资源
./docker-run.sh update [tag]           # 更新镜像
```

## 🌟 使用示例

### 场景1: 开发测试
```bash
./docker-run.sh login
./docker-run.sh run latest dev
./docker-run.sh logs
```

### 场景2: 生产部署
```bash
./docker-run.sh login
./docker-run.sh pull v1.0.0
./docker-run.sh run v1.0.0 prod
```

### 场景3: 版本更新
```bash
./docker-run.sh update v1.1.0
```

## 🔗 访问地址

- **应用主页**: http://localhost:8091
- **健康检查**: http://localhost:8091/actuator/health
- **API文档**: http://localhost:8091/swagger-ui.html (如果配置了Swagger)

## ⚠️ 常见问题

**端口占用**
```bash
# 查看端口占用
lsof -i :8091

# 修改端口映射
docker run -p 9091:8091 ...
```

**容器名冲突**
```bash
# 停止并删除现有容器
docker stop bhuang-bigmark-app
docker rm bhuang-bigmark-app
```

**镜像更新**
```bash
# 强制拉取最新镜像
docker pull crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest --no-cache
```

## 🛟 获取帮助

- 查看容器日志：`./docker-run.sh logs`
- 检查容器状态：`./docker-run.sh status`
- GitHub Issues: [项目Issues页面]
- 查看完整文档：`.github/workflows/README.md`