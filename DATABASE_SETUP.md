# Discord 音樂機器人 - 數據庫設置指南

## 📋 需要提供的 MySQL 連接資訊

在 `src/main/resources/application.properties` 中，請更新以下配置：

```properties
# 數據庫連接配置
spring.datasource.url=jdbc:mysql://localhost:3306/discord_music_bot?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 需要提供的資訊：
1. **MySQL 伺服器地址**: 預設為 `localhost:3306`
2. **數據庫名稱**: 預設為 `discord_music_bot`
3. **用戶名**: 請替換 `your_mysql_username`
4. **密碼**: 請替換 `your_mysql_password`

## 🔧 數據庫設置步驟

### 1. 創建數據庫
```sql
CREATE DATABASE discord_music_bot;
USE discord_music_bot;
```

### 2. 執行數據表創建腳本
執行 `src/main/resources/sql/user_playlists.sql` 中的 SQL 腳本：

```sql
-- 用戶播放清單資料表
CREATE TABLE IF NOT EXISTS playlist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL COMMENT 'Discord 用戶 ID',
    playlist_name VARCHAR(100) NOT NULL COMMENT '播放清單名稱',
    song_title VARCHAR(500) NOT NULL COMMENT '歌曲標題',
    song_url VARCHAR(2000) NOT NULL COMMENT '歌曲 URL',
    duration BIGINT DEFAULT 0 COMMENT '歌曲時長（毫秒）',
    song_order INT DEFAULT 1 COMMENT '歌曲在播放清單中的順序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    
    INDEX idx_user_id (user_id),
    INDEX idx_playlist_name (playlist_name),
    INDEX idx_user_playlist (user_id, playlist_name),
    INDEX idx_song_order (user_id, playlist_name, song_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='播放清單項目表';
```

### 3. 驗證數據庫連接
啟動應用程序後，檢查日誌中是否有數據庫連接錯誤。

## 🎵 播放清單功能使用說明

### 可用指令

| 指令 | 說明 | 範例 |
|------|------|------|
| `!playlist create <名稱> <網址>` | 創建新的播放清單 | `!playlist create 我的最愛 https://youtu.be/dQw4w9WgXcQ` |
| `!playlist add <名稱> <網址>` | 添加歌曲到播放清單 | `!playlist add 我的最愛 https://youtu.be/fJ9rUzIMcZQ` |
| `!playlist list` | 列出你的所有播放清單 | `!playlist list` |
| `!playlist show <名稱>` | 查看播放清單內容 | `!playlist show 我的最愛` |
| `!playlist play <名稱>` | 播放整個播放清單 | `!playlist play 我的最愛` |
| `!playlist remove <名稱> <序號>` | 移除播放清單中的歌曲 | `!playlist remove 我的最愛 2` |
| `!playlist delete <名稱>` | 刪除播放清單 | `!playlist delete 我的最愛` |

### 指令解析改進
- **支援缺少空格的指令**: `!playhttps://youtu.be/dQw4w9WgXcQ` 會自動解析為 `!play https://youtu.be/dQw4w9WgXcQ`
- **智能 URL 識別**: 自動識別 YouTube、SoundCloud 等平台的 URL
- **多語言支持**: 支援中文播放清單名稱

## 🧪 測試數據庫功能

### 1. 運行單元測試
```bash
mvn test
```

### 2. 手動測試步驟
1. 啟動機器人
2. 在 Discord 中使用指令：
   ```
   !playlist create 測試清單 https://www.youtube.com/watch?v=dQw4w9WgXcQ
   !playlist list
   !playlist show 測試清單
   !playlist add 測試清單 https://www.youtube.com/watch?v=fJ9rUzIMcZQ
   !playlist play 測試清單
   ```

### 3. 檢查數據庫
```sql
SELECT * FROM playlist_items ORDER BY created_at DESC;
```

## ⚠️ 故障排除

### 常見問題

1. **連接失敗**: 檢查 MySQL 服務是否啟動，用戶名密碼是否正確
2. **數據表不存在**: 確保已執行創建表的 SQL 腳本
3. **編碼問題**: 確保數據庫使用 UTF-8 編碼
4. **權限問題**: 確保 MySQL 用戶有足夠權限操作數據庫

### 日誌檢查
查看應用程序日誌中的錯誤訊息：
```
logging.level.com.coco.bot.service=DEBUG
```

## 🔒 安全注意事項

1. **密碼安全**: 不要將數據庫密碼提交到版本控制系統
2. **SQL 注入防護**: 代碼中使用 PreparedStatement 防止 SQL 注入
3. **連接池**: 使用 HikariCP 連接池管理數據庫連接
4. **備份**: 定期備份播放清單數據