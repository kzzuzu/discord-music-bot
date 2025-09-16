-- Discord Music Bot Database Schema
-- 請先創建數據庫：CREATE DATABASE discord_music_bot;

USE discord_music_bot;

-- 歌曲信息表
CREATE TABLE IF NOT EXISTS songs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    duration BIGINT DEFAULT 0,
    platform VARCHAR(50) DEFAULT 'YouTube',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_title (title(100)),
    INDEX idx_url (url(200))
);

-- 伺服器設定表
CREATE TABLE IF NOT EXISTS guild_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guild_id VARCHAR(20) NOT NULL UNIQUE,
    guild_name VARCHAR(100) NOT NULL,
    default_volume INT DEFAULT 50,
    max_queue_size INT DEFAULT 50,
    allow_duplicates BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_guild_id (guild_id)
);

-- 用戶統計表
CREATE TABLE IF NOT EXISTS user_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    username VARCHAR(100) NOT NULL,
    total_plays INT DEFAULT 0,
    total_duration BIGINT DEFAULT 0,
    favorite_platform VARCHAR(50) DEFAULT 'YouTube',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_user_guild (user_id, guild_id),
    INDEX idx_user_id (user_id),
    INDEX idx_guild_id (guild_id)
);

-- 播放歷史表
CREATE TABLE IF NOT EXISTS play_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guild_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    username VARCHAR(100) NOT NULL,
    song_id BIGINT,
    song_title VARCHAR(500) NOT NULL,
    song_url VARCHAR(2000) NOT NULL,
    song_duration BIGINT DEFAULT 0,
    platform VARCHAR(50) DEFAULT 'YouTube',
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE SET NULL,
    INDEX idx_guild_id (guild_id),
    INDEX idx_user_id (user_id),
    INDEX idx_played_at (played_at),
    INDEX idx_song_id (song_id)
);

-- 播放列表表
CREATE TABLE IF NOT EXISTS playlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    creator_id VARCHAR(20) NOT NULL,
    creator_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_guild_id (guild_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_public (is_public)
);

-- 播放列表歌曲關聯表
CREATE TABLE IF NOT EXISTS playlist_songs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    playlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    position INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_playlist_position (playlist_id, position),
    INDEX idx_playlist_id (playlist_id),
    INDEX idx_song_id (song_id)
);

-- 插入一些初始數據
INSERT INTO guild_settings (guild_id, guild_name) VALUES 
('default', 'Default Server') 
ON DUPLICATE KEY UPDATE guild_name = VALUES(guild_name);