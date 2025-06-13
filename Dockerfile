FROM openjdk:17-jre-slim

# 设置维护者信息
LABEL maintainer="bhuang <your-email@example.com>"
LABEL description="Bhuang-BigMark Spring Boot Application"

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户（安全最佳实践）
RUN groupadd -r spring && useradd -r -g spring spring

# 设置工作目录
WORKDIR /app

# 复制应用程序jar文件
COPY Bhuang-BigMark-app/target/*.jar app.jar

# 更改文件所有者
RUN chown -R spring:spring /app

# 切换到非root用户
USER spring

# 暴露端口
EXPOSE 8091

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8091/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]