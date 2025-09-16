package com.coco.bot.dao;

import com.coco.bot.entity.PlaylistItem;
import java.util.List;

/**
 * 播放清單數據訪問物件介面
 * 定義播放清單相關的數據庫操作
 */
public interface PlaylistDao {

    /**
     * 創建播放清單項目
     *
     * @param playlistItem 播放清單項目
     * @return 保存成功返回 true
     */
    boolean save(PlaylistItem playlistItem);

    /**
     * 檢查播放清單是否存在
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @return 存在返回 true
     */
    boolean existsByUserIdAndPlaylistName(String userId, String playlistName);

    /**
     * 獲取用戶的所有播放清單名稱
     *
     * @param userId 用戶ID
     * @return 播放清單名稱列表
     */
    List<String> findDistinctPlaylistNamesByUserId(String userId);

    /**
     * 獲取播放清單中的所有歌曲
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @return 歌曲列表
     */
    List<PlaylistItem> findByUserIdAndPlaylistNameOrderBySongOrder(String userId, String playlistName);

    /**
     * 獲取播放清單中的最大順序號
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @return 最大順序號
     */
    Integer findMaxSongOrderByUserIdAndPlaylistName(String userId, String playlistName);

    /**
     * 刪除整個播放清單
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @return 刪除成功返回 true
     */
    boolean deleteByUserIdAndPlaylistName(String userId, String playlistName);

    /**
     * 刪除播放清單中的特定歌曲
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @param songOrder 歌曲順序
     * @return 刪除成功返回 true
     */
    boolean deleteBySongOrder(String userId, String playlistName, Integer songOrder);

    /**
     * 重新排序歌曲（當刪除歌曲後）
     *
     * @param userId 用戶ID
     * @param playlistName 播放清單名稱
     * @param deletedOrder 被刪除的歌曲順序
     * @return 重新排序成功返回 true
     */
    boolean reorderSongsAfterDeletion(String userId, String playlistName, Integer deletedOrder);
}