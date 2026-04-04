# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# 先複製 pom.xml 下載依賴（利用 Docker 快取層）
COPY pom.xml .
RUN mvn dependency:go-offline -q

# 複製原始碼並打包（跳過測試）
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 建立非 root 使用者執行應用程式
# /app/data 用於存放 oauth2.json 等執行期產生的持久化資料
RUN addgroup -S botgroup && adduser -S botuser -G botgroup && \
    mkdir -p /app/logs /app/data && chown -R botuser:botgroup /app

# 從 builder 複製 JAR
COPY --from=builder /build/target/discordMusicBot-1.0-SNAPSHOT.jar app.jar

RUN chown botuser:botgroup app.jar
USER botuser

# -Duser.dir=/app/data：讓 oauth2.json 寫入 /app/data（掛載 volume 後可跨容器重建保存）
ENTRYPOINT ["java", "-Duser.dir=/app/data", "-jar", "/app/app.jar"]
