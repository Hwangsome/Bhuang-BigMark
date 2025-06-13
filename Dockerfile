FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="bhuang <hwangb66@163.com>"
LABEL description="Bhuang-BigMark 大营销平台系统"
LABEL version="1.0-SNAPSHOT"

# 设置工作目录
WORKDIR /app

# 时区设置
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8"
ENV PARAMS=""

# 复制应用程序jar文件
COPY Bhuang-BigMark-app/target/Bhuang-BigMark-app.jar app.jar

# 暴露端口
EXPOSE 8091

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $PARAMS"]