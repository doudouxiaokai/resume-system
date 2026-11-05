# 使用阿里云镜像，解决国内访问问题
# 构建阶段
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# 使用阿里云Maven镜像加速
RUN mvn dependency:go-offline -s settings.xml || true
COPY src ./src
# 使用阿里云镜像构建
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# 运行阶段 - 使用更小的基础镜像
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从构建阶段复制jar文件
COPY --from=build /app/target/*.jar app.jar

# ====== 以下是针对简历系统的必要补充 ======

# 1. 设置时区（Alpine需要安装tzdata包）
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 2. 创建必要的目录结构（与 application.properties 中的路径保持一致）
RUN mkdir -p /home/student9/uploads9 && \
    mkdir -p /home/student9/logs9 && \
    mkdir -p /home/student9/tmp9 && \
    mkdir -p /opt/student-projects/student9/uploads && \
    mkdir -p /var/log/resume-system && \
    chmod -R 755 /home/student9/uploads9 && \
    chmod -R 755 /home/student9/logs9 && \
    chmod -R 755 /home/student9/tmp9

# 3. 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

# 4. 设置目录所有者（必须在切换用户前完成）
RUN chown -R spring:spring /app && \
    chown -R spring:spring /home/student9 && \
    chown -R spring:spring /opt/student-projects && \
    chown -R spring:spring /var/log/resume-system

# 切换到非root用户
USER spring

# 暴露端口（修改为8088，因为你的端口是8088）
EXPOSE 8088

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]