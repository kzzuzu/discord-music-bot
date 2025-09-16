package com.coco.bot.dao.impl;

import com.coco.bot.dao.PlaylistDao;
import com.coco.bot.entity.PlaylistItem;
import com.coco.bot.repository.PlaylistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 播放清單數據訪問物件實現類
 * 使用 Spring Data JPA Repository 實現數據庫操作
 */
@Repository
public class PlaylistDaoImpl implements PlaylistDao {

    private final PlaylistItemRepository playlistItemRepository;

    @Autowired
    public PlaylistDaoImpl(PlaylistItemRepository playlistItemRepository) {
        this.playlistItemRepository = playlistItemRepository;
    }

    @Override
    public boolean save(PlaylistItem playlistItem) {
        try {
            playlistItemRepository.save(playlistItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean existsByUserIdAndPlaylistName(String userId, String playlistName) {
        return playlistItemRepository.existsByUserIdAndPlaylistName(userId, playlistName);
    }

    @Override
    public List<String> findDistinctPlaylistNamesByUserId(String userId) {
        return playlistItemRepository.findDistinctPlaylistNamesByUserId(userId);
    }

    @Override
    public List<PlaylistItem> findByUserIdAndPlaylistNameOrderBySongOrder(String userId, String playlistName) {
        return playlistItemRepository.findByUserIdAndPlaylistNameOrderBySongOrder(userId, playlistName);
    }

    @Override
    public Integer findMaxSongOrderByUserIdAndPlaylistName(String userId, String playlistName) {
        return playlistItemRepository.findMaxSongOrderByUserIdAndPlaylistName(userId, playlistName);
    }

    @Override
    public boolean deleteByUserIdAndPlaylistName(String userId, String playlistName) {
        try {
            playlistItemRepository.deleteByUserIdAndPlaylistName(userId, playlistName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteBySongOrder(String userId, String playlistName, Integer songOrder) {
        try {
            playlistItemRepository.deleteBySongOrder(userId, playlistName, songOrder);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean reorderSongsAfterDeletion(String userId, String playlistName, Integer deletedOrder) {
        try {
            playlistItemRepository.reorderSongsAfterDeletion(userId, playlistName, deletedOrder);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}