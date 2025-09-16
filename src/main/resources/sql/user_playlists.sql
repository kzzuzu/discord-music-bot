-- Discord Music Bot - User Playlists Table
-- 請先創建數據庫：CREATE DATABASE discord_music_bot;

USE discord_music_bot;

-- 用戶播放清單資料表
CREATE TABLE IF NOT EXISTS user_playlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL COMMENT 'Discord 用戶 ID',
    playlist_name VARCHAR(100) NOT NULL COMMENT '播放清單名稱',
    song_title VARCHAR(500) NOT NULL COMMENT '歌曲標題',
    song_url TEXT NOT NULL COMMENT '歌曲 URL',
    duration BIGINT DEFAULT 0 COMMENT '歌曲時長（毫秒）',
    song_order INT DEFAULT 1 COMMENT '歌曲在播放清單中的順序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    
    -- 索引優化查詢性能
    INDEX idx_user_id (user_id),
    INDEX idx_playlist_name (playlist_name),
    INDEX idx_user_playlist (user_id, playlist_name),
    INDEX idx_song_order (user_id, playlist_name, song_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶播放清單表';

-- 插入一些示例數據（可選）
-- INSERT INTO user_playlists (user_id, playlist_name, song_title, song_url, duration, song_order) VALUES
-- ('123456789012345678', 'My Favorites', 'Never Gonna Give You Up', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 212000, 1),
-- ('123456789012345678', 'My Favorites', 'Bohemian Rhapsody', 'https://www.youtube.com/watch?v=fJ9rUzIMcZQ', 355000, 2),
-- ('987654321098765432', 'Chill Music', 'Lofi Hip Hop', 'https://www.youtube.com/watch?v=5qap5aO4i9A', 120000, 1);