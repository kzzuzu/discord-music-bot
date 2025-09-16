package com.coco.bot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 播放清單項目實體類
 */
@Entity
@Table(name = "playlist_items")
public class PlaylistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "playlist_name", nullable = false)
    private String playlistName;

    @Column(name = "song_title", nullable = false)
    private String songTitle;

    @Column(name = "song_url", nullable = false, length = 2000)
    private String songUrl;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "song_order")
    private Integer songOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 無參數建構子
    public PlaylistItem() {}

    // 完整建構子
    public PlaylistItem(String userId, String playlistName, String songTitle, String songUrl, Long duration, Integer songOrder) {
        this.userId = userId;
        this.playlistName = playlistName;
        this.songTitle = songTitle;
        this.songUrl = songUrl;
        this.duration = duration;
        this.songOrder = songOrder;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getSongOrder() {
        return songOrder;
    }

    public void setSongOrder(Integer songOrder) {
        this.songOrder = songOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PlaylistItem{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", playlistName='" + playlistName + '\'' +
                ", songTitle='" + songTitle + '\'' +
                ", songUrl='" + songUrl + '\'' +
                ", duration=" + duration +
                ", songOrder=" + songOrder +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}