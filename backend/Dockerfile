# 后端 Dockerfile - 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 复制 pom.xml 先下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 复制构建产物
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
