# 🎵 Discord 音樂機器人

一個功能強大的 Discord 音樂機器人，支援 YouTube 音樂播放和多種音源。使用 Java 開發，基於 JDA（Java Discord API）和 LavaPlayer 音頻框架。

## ✨ 特色功能

### 🎯 核心功能
- **YouTube 音樂播放**：使用 yt-dlp 技術，穩定播放 YouTube 音樂
- **多音源支援**：支援 YouTube、SoundCloud、Bandcamp 等多種音源
- **音樂控制**：播放、暫停、停止等基本控制功能
- **自動語音頻道加入**：機器人會自動加入用戶所在的語音頻道

### 🔧 技術特色
- **先進的 YouTube 支援**：使用 yt-dlp 繞過反機器人保護
- **高品質音頻**：自動選擇最佳音頻品質
- **非阻塞設計**：YouTube 解析在獨立線程中進行，不會阻塞機器人
- **詳細錯誤處理**：提供清晰的錯誤訊息和使用建議

## 🚀 快速開始

### 系統需求

- **Java 11 或更高版本**
- **Python 3.7+**
- **yt-dlp**（用於 YouTube 支援）
- **Maven**（用於專案建置）

### 安裝步驟

1. **克隆專案**
   ```bash
   git clone <repository-url>
   cd discordMusicBot
   ```

2. **安裝 Python 依賴**
   ```bash
   pip install yt-dlp
   ```

3. **配置 Discord Bot Token**
   - 前往 [Discord Developer Portal](https://discord.com/developers/applications)
   - 創建新的應用程式和 Bot
   - 複製 Bot Token
   - 在 `DiscordBot.java` 中替換 Token（第 107 行）

4. **編譯專案**
   ```bash
   mvn clean compile
   ```

5. **運行機器人**
   ```bash
   mvn exec:java -Dexec.mainClass="com.coco.bot.DiscordBot"
   ```

   或在 IntelliJ IDEA 中：
   - 打開 `src/main/java/com/coco/bot/DiscordBot.java`
   - 點擊 `main` 方法旁的綠色播放按鈕

## 🎮 使用指南

### 基本指令

| 指令 | 說明 | 範例 |
|------|------|------|
| `!play <網址>` | 播放音樂 | `!play https://www.youtube.com/watch?v=dQw4w9WgXcQ` |
| `!stop` | 停止播放 | `!stop` |
| `!pause` | 暫停播放 | `!pause` |
| `!resume` | 恢復播放 | `!resume` |
| `!help` | 顯示幫助訊息 | `!help` |

### 支援的音源

- **YouTube**：`https://www.youtube.com/watch?v=...` 或 `https://youtu.be/...`
- **SoundCloud**：`https://soundcloud.com/...`
- **Bandcamp**：`https://artist.bandcamp.com/...`
- **直接音頻連結**：`.mp3`、`.wav`、`.flac` 等

### 使用範例

1. **播放 YouTube 音樂**
   ```
   !play https://www.youtube.com/watch?v=dQw4w9WgXcQ
   ```

2. **播放 SoundCloud 音樂**
   ```
   !play https://soundcloud.com/artist/track-name
   ```

3. **音樂控制**
   ```
   !pause    # 暫停當前播放
   !resume   # 恢復播放
   !stop     # 停止播放
   ```

## 🏗️ 專案架構

### 核心類別

#### `DiscordBot.java`
- 主要機器人類，處理 Discord 事件
- 管理音頻播放器和指令處理
- 實現語音頻道連接和音樂控制

#### `YouTubeResolver.java`
- YouTube URL 解析器
- 使用 yt-dlp 獲取直接音頻串流連結
- 處理影片標題和時長資訊

#### `AudioPlayerSendHandler.java`
- 音頻發送處理器
- 將 LavaPlayer 音頻數據轉換為 Discord 格式
- 管理音頻緩衝區和 Opus 編碼

### 技術堆疊

- **JDA 5.1.2**：Discord API 客戶端
- **LavaPlayer 1.4.3**：音頻播放框架
- **Lavalink YouTube Plugin**：增強型 YouTube 支援
- **yt-dlp**：YouTube 解析工具
- **Maven**：專案建置工具

## ⚙️ 配置選項

### Bot Token 配置

**⚠️ 重要安全提醒**：在生產環境中，不要將 Bot Token 直接寫在程式碼中！

建議使用環境變數：

```java
String token = System.getenv("DISCORD_BOT_TOKEN");
if (token == null) {
    throw new IllegalStateException("請設置 DISCORD_BOT_TOKEN 環境變數");
}
```

### 音頻品質設定

在 `YouTubeResolver.java` 中可以調整音頻品質：

```java
command.add("--format");
command.add("bestaudio/best");  // 最佳音頻品質
// 或
command.add("worstaudio");      // 最低音頻品質（節省頻寬）
```

## 🔧 疑難排解

### 常見問題

**Q: YouTube 影片無法播放？**
- A: 確認 yt-dlp 已正確安裝：`python -m yt_dlp --version`
- A: 檢查影片是否為私人影片或地區限制
- A: 嘗試更新 yt-dlp：`pip install --upgrade yt-dlp`

**Q: 機器人無法加入語音頻道？**
- A: 確認機器人有 "Connect" 和 "Speak" 權限
- A: 檢查用戶是否在語音頻道中
- A: 確認機器人已被邀請到伺服器

**Q: 編譯時出現依賴問題？**
- A: 執行 `mvn clean install` 重新下載依賴
- A: 檢查網路連接，確保能訪問 Maven 倉庫

### 日志分析

機器人會輸出詳細的日志訊息：

```
🎵 Discord 音樂機器人啟動成功！
Successfully registered enhanced YouTube source manager
支援指令：!play, !stop, !pause, !resume, !help
```

如果看到錯誤訊息，請檢查：
1. Bot Token 是否正確
2. 網路連接是否正常
3. 依賴是否完整安裝

## 🤝 貢獻指南

歡迎提交問題和功能請求！

### 開發流程

1. Fork 專案
2. 創建功能分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add some amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 開啟 Pull Request

### 編碼規範

- 使用 Java 11+ 語法
- 添加適當的註解和文檔
- 遵循現有的程式碼風格
- 確保所有方法都有適當的錯誤處理

## 📝 更新日志

### v1.0.0 (2025-08-25)
- ✨ 初始版本發布
- 🎵 支援 YouTube 音樂播放
- 🔧 集成 yt-dlp 技術
- 🎮 基本音樂控制功能
- 📚 完整的程式碼文檔

## 📄 授權條款

本專案使用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 文件

## 🙏 致謝

- [JDA](https://github.com/DV8FromTheWorld/JDA) - Java Discord API
- [LavaPlayer](https://github.com/sedmelluq/lavaplayer) - 音頻播放框架
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) - YouTube 下載工具
- [Lavalink](https://github.com/freyacodes/Lavalink) - 音頻服務器

## 📞 支援與聯繫

如果您在使用過程中遇到問題，請：

1. 查看本 README 的疑難排解章節
2. 搜索已有的 Issues
3. 提交新的 Issue 描述問題

---

**享受音樂！** 🎶