package com.coco.bot.repository;

import com.coco.bot.entity.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    /**
     * 檢查播放清單是否存在
     */
    boolean existsByUserIdAndPlaylistName(String userId, String playlistName);

    /**
     * 獲取用戶的所有播放清單名稱（去重）
     */
    @Query("SELECT DISTINCT p.playlistName FROM PlaylistItem p WHERE p.userId = :userId ORDER BY p.playlistName")
    List<String> findDistinctPlaylistNamesByUserId(@Param("userId") String userId);

    /**
     * 獲取播放清單中的所有歌曲，按順序排列
     */
    List<PlaylistItem> findByUserIdAndPlaylistNameOrderBySongOrder(String userId, String playlistName);

    /**
     * 獲取播放清單中的最大順序號
     */
    @Query("SELECT COALESCE(MAX(p.songOrder), 0) FROM PlaylistItem p WHERE p.userId = :userId AND p.playlistName = :playlistName")
    Integer findMaxSongOrderByUserIdAndPlaylistName(@Param("userId") String userId, @Param("playlistName") String playlistName);

    /**
     * 刪除整個播放清單
     */
    @Modifying
    @Query("DELETE FROM PlaylistItem p WHERE p.userId = :userId AND p.playlistName = :playlistName")
    void deleteByUserIdAndPlaylistName(@Param("userId") String userId, @Param("playlistName") String playlistName);

    /**
     * 刪除播放清單中的特定歌曲
     */
    @Modifying
    @Query("DELETE FROM PlaylistItem p WHERE p.userId = :userId AND p.playlistName = :playlistName AND p.songOrder = :songOrder")
    void deleteBySongOrder(@Param("userId") String userId, @Param("playlistName") String playlistName, @Param("songOrder") Integer songOrder);

    /**
     * 重新排序歌曲（當刪除歌曲後）
     */
    @Modifying
    @Query("UPDATE PlaylistItem p SET p.songOrder = p.songOrder - 1 " +
           "WHERE p.userId = :userId AND p.playlistName = :playlistName AND p.songOrder > :deletedOrder")
    void reorderSongsAfterDeletion(@Param("userId") String userId, @Param("playlistName") String playlistName, @Param("deletedOrder") Integer deletedOrder);
}