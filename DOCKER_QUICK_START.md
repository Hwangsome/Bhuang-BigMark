# 🚀 Docker 快速开始指南

## 📋 系统要求

- **Docker Engine** 20.10+ 
- **系统架构支持**：
  - ✅ Intel/AMD (linux/amd64)
  - ✅ Apple Silicon M1/M2 (linux/arm64)
- **网络访问**：确保能访问阿里云镜像仓库

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

## 🔄 常见问题

### 端口冲突
```bash
# 错误: port is already in use
# 解决: 更改映射端口
docker run -p 8092:8091 ...
```

### 容器名称冲突
```bash
# 错误: name is already in use
# 解决: 停止现有容器或使用不同名称
docker stop bhuang-bigmark-app
docker rm bhuang-bigmark-app
```

### 镜像更新
```bash
# 拉取最新镜像
./docker-run.sh pull latest
./docker-run.sh update latest
```

### ARM64/Apple Silicon 支持
如果遇到 "no matching manifest for linux/arm64/v8" 错误：

```bash
# 1. 检查镜像是否支持您的架构
docker manifest inspect crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest

# 2. 强制拉取特定架构（如果需要）
docker pull --platform linux/amd64 crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com/bhuang-repo/bhuang-bigmark:latest

# 3. 使用管理脚本（会自动处理架构问题）
./docker-run.sh run
```

**说明**：
- 🍎 **Apple Silicon (M1/M2)**：支持原生 ARM64 镜像
- 💻 **Intel Mac/PC**：支持 AMD64 镜像  
- 🔄 **自动适配**：Docker 会自动选择匹配的架构

## 🛟 获取帮助

- 查看容器日志：`./docker-run.sh logs`
- 检查容器状态：`./docker-run.sh status`
- GitHub Issues: [项目Issues页面]
- 查看完整文档：`.github/workflows/README.md`