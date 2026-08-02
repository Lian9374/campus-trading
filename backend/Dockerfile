# === 阶段1: 构建 (context = backend/) ===
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true
COPY src ./src
RUN mvn package -DskipTests -q -f pom.xml

# === 阶段2: 运行 ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# MaxRAMPercentage: 让 JVM 使用容器内存的 75% (Render 免费层 512MB), 避免 OOM
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Xss512k", "-jar", "app.jar"]
