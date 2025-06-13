#!/bin/bash

# Docker 镜像管理脚本 - Bhuang-BigMark
# 支持阿里云容器镜像服务的一键部署和管理

# 配置变量
REGISTRY="crpi-wzl2k45d0lxbiagj.cn-shenzhen.personal.cr.aliyuncs.com"
REPOSITORY="bhuang-repo/bhuang-bigmark"
CONTAINER_NAME="bhuang-bigmark-app"
PORT="8091"
DEFAULT_TAG="latest"
DEFAULT_PROFILE="dev"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检测系统架构
ARCH=$(uname -m)
if [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "aarch64" ]]; then
    PLATFORM_INFO="🍎 检测到 Apple Silicon (ARM64) 架构"
elif [[ "$ARCH" == "x86_64" ]]; then
    PLATFORM_INFO="💻 检测到 Intel (AMD64) 架构"
else
    PLATFORM_INFO="❓ 检测到 $ARCH 架构"
fi

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 打印帮助信息
print_help() {
    echo -e "${BLUE}=== Bhuang-BigMark Docker 管理脚本 ===${NC}"
    echo -e "${PLATFORM_INFO}"
    echo ""
    echo "用法: $0 <命令> [参数]"
    echo ""
    echo "命令:"
    echo "  login                    登录阿里云 Docker Registry"
    echo "  pull [tag]              拉取镜像 (默认: latest)"
    echo "  run [tag] [profile]     运行容器 (默认: latest, dev)"
    echo "  stop                    停止容器"
    echo "  restart                 重启容器"
    echo "  logs                    查看容器日志"
    echo "  status                  查看容器状态"
    echo "  clean                   清理停止的容器和镜像"
    echo "  update [tag]            更新到最新镜像"
    echo "  help                    显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 login                           # 登录阿里云"
    echo "  $0 pull                            # 拉取最新镜像"
    echo "  $0 pull v1.0.0                     # 拉取指定版本"
    echo "  $0 run                             # 运行开发环境"
    echo "  $0 run latest prod                 # 运行生产环境"
    echo "  $0 logs                            # 查看日志"
    echo "  $0 update                          # 更新到最新版本"
}

# 登录阿里云 Docker Registry
docker_login() {
    print_info "登录阿里云 Docker Registry..."
    docker login --username=黄帅啊 ${REGISTRY}
    if [ $? -eq 0 ]; then
        print_success "登录成功！"
    else
        print_error "登录失败！"
        exit 1
    fi
}

# 拉取镜像
pull_image() {
    local tag=${1:-$DEFAULT_TAG}
    local image="${REGISTRY}/${REPOSITORY}:${tag}"
    
    print_info "拉取镜像: ${image}"
    docker pull ${image}
    if [ $? -eq 0 ]; then
        print_success "镜像拉取成功！"
    else
        print_error "镜像拉取失败！"
        exit 1
    fi
}

# 运行容器
run_container() {
    local tag=${1:-$DEFAULT_TAG}
    local profile=${2:-dev}
    local image="${REGISTRY}/${REPOSITORY}:${tag}"
    
    # 检查容器是否已经运行
    if docker ps -q -f name=${CONTAINER_NAME} | grep -q .; then
        print_warning "容器 ${CONTAINER_NAME} 已在运行中"
        print_info "使用 '$0 stop' 停止现有容器，或 '$0 restart' 重启"
        return 1
    fi
    
    # 清理已停止的同名容器
    if docker ps -a -q -f name=${CONTAINER_NAME} | grep -q .; then
        print_info "清理已停止的容器..."
        docker rm ${CONTAINER_NAME}
    fi
    
    print_info "启动容器: ${CONTAINER_NAME}"
    print_info "使用镜像: ${image}"
    print_info "运行环境: ${profile}"
    
    if [ "${profile}" = "prod" ]; then
        # 生产环境配置
        docker run -d \
            --name ${CONTAINER_NAME} \
            -p ${PORT}:8091 \
            -e SPRING_PROFILES_ACTIVE=prod \
            -e JAVA_OPTS='-Xms1g -Xmx2g -Dfile.encoding=UTF-8' \
            --restart unless-stopped \
            ${image}
    else
        # 开发环境配置
        docker run -d \
            --name ${CONTAINER_NAME} \
            -p ${PORT}:8091 \
            -e SPRING_PROFILES_ACTIVE=dev \
            ${image}
    fi
    
    if [ $? -eq 0 ]; then
        print_success "容器启动成功！"
        print_info "访问地址: http://localhost:${PORT}"
        print_info "健康检查: http://localhost:${PORT}/actuator/health"
        print_info "查看日志: $0 logs"
    else
        print_error "容器启动失败！"
        exit 1
    fi
}

# 停止容器
stop_container() {
    print_info "停止容器: ${CONTAINER_NAME}"
    docker stop ${CONTAINER_NAME}
    if [ $? -eq 0 ]; then
        print_success "容器已停止"
    else
        print_warning "容器可能未在运行"
    fi
}

# 重启容器
restart_container() {
    print_info "重启容器: ${CONTAINER_NAME}"
    docker restart ${CONTAINER_NAME}
    if [ $? -eq 0 ]; then
        print_success "容器重启成功！"
    else
        print_error "容器重启失败！"
    fi
}

# 查看日志
show_logs() {
    print_info "显示容器日志 (按 Ctrl+C 退出):"
    docker logs -f ${CONTAINER_NAME}
}

# 查看状态
show_status() {
    echo "=== 容器状态 ==="
    docker ps -a --filter name=${CONTAINER_NAME} --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
    echo "=== 镜像信息 ==="
    docker images --filter reference="${REGISTRY}/${REPOSITORY}" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
}

# 清理
clean_up() {
    print_info "清理停止的容器和未使用的镜像..."
    
    # 删除停止的容器
    if docker ps -a -q -f name=${CONTAINER_NAME} -f status=exited | grep -q .; then
        docker rm ${CONTAINER_NAME}
        print_success "已清理停止的容器"
    fi
    
    # 清理未使用的镜像
    docker image prune -f
    print_success "清理完成"
}

# 更新镜像
update_image() {
    local tag=${1:-$DEFAULT_TAG}
    
    print_info "更新镜像到版本: ${tag}"
    
    # 停止容器
    if docker ps -q -f name=${CONTAINER_NAME} | grep -q .; then
        stop_container
    fi
    
    # 拉取新镜像
    pull_image ${tag}
    
    # 启动新容器
    run_container ${tag} "dev"
}

# 主函数
main() {
    case $1 in
        login)
            docker_login
            ;;
        pull)
            pull_image $2
            ;;
        run)
            run_container $2 $3
            ;;
        stop)
            stop_container
            ;;
        restart)
            restart_container
            ;;
        logs)
            show_logs
            ;;
        status)
            show_status
            ;;
        clean)
            clean_up
            ;;
        update)
            update_image $2
            ;;
        help|--help|-h)
            print_help
            ;;
        *)
            if [ -z "$1" ]; then
                print_help
            else
                print_error "未知命令: $1"
                echo ""
                print_help
                exit 1
            fi
            ;;
    esac
}

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    print_error "Docker 未安装或未在 PATH 中找到"
    exit 1
fi

# 执行主函数
main "$@"