# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build

# 先複製 pom.xml 下載依賴（利用 Docker 快取層）
COPY pom.xml .
RUN mvn dependency:go-offline -q

# 複製原始碼並打包（跳過測試）
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Stage 2: Runtime ----
# 使用 Debian-based（非 Alpine）避免 glibc native 庫相容問題（DAVE E2EE、LavaPlayer natives）
FROM eclipse-temurin:25-jre
WORKDIR /app

# 安裝必要套件：
#   python3 + yt-dlp：B 站等平台音頻解析
#   libstdc++6, libgcc-s1：JDA DAVE E2EE native library (mlspp) 所需 C++ runtime
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        python3 \
        python3-pip \
        libstdc++6 \
        libgcc-s1 && \
    pip3 install --no-cache-dir --break-system-packages yt-dlp && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 建立非 root 使用者執行應用程式
# /app/data 用於存放 oauth2.json 等執行期產生的持久化資料
RUN groupadd -r botgroup && useradd -r -g botgroup botuser && \
    mkdir -p /app/logs /app/data && chown -R botuser:botgroup /app

# 從 builder 複製 JAR
COPY --from=builder /build/target/discordMusicBot-1.0-SNAPSHOT.jar app.jar

RUN chown botuser:botgroup app.jar
USER botuser

# -Duser.dir=/app/data：讓 oauth2.json 寫入 /app/data（掛載 volume 後可跨容器重建保存）
ENTRYPOINT ["java", "-Duser.dir=/app/data", "-jar", "/app/app.jar"]
