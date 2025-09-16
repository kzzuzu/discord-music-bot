package com.coco.bot.service;

import com.coco.bot.dao.PlaylistDao;
import com.coco.bot.entity.PlaylistItem;
import com.coco.bot.handler.YouTubeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PlaylistService 單元測試
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistDao mockPlaylistDao;

    @Mock
    private YouTubeResolver mockYouTubeResolver;

    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playlistService = new PlaylistService(mockPlaylistDao, mockYouTubeResolver);
    }

    @Test
    @DisplayName("創建播放清單應該成功")
    void shouldCreatePlaylistSuccessfully() {
        // Arrange
        when(mockPlaylistDao.save(any(PlaylistItem.class))).thenReturn(true);

        // Act
        boolean result = playlistService.createPlaylist("123456", "My Playlist", "Test Song", "http://example.com", 180000L);

        // Assert
        assertTrue(result, "創建播放清單應該成功");
        verify(mockPlaylistDao).save(any(PlaylistItem.class));
    }

    @Test
    @DisplayName("檢查播放清單存在應該返回正確結果")
    void shouldCheckPlaylistExistsCorrectly() {
        // Arrange
        when(mockPlaylistDao.existsByUserIdAndPlaylistName("123456", "My Playlist")).thenReturn(true);

        // Act
        boolean result = playlistService.playlistExists("123456", "My Playlist");

        // Assert
        assertTrue(result, "播放清單存在時應該返回 true");
        verify(mockPlaylistDao).existsByUserIdAndPlaylistName("123456", "My Playlist");
    }

    @Test
    @DisplayName("獲取用戶播放清單應該返回正確的清單")
    void shouldReturnUserPlaylists() {
        // Arrange
        List<String> expectedPlaylists = Arrays.asList("Playlist 1", "Playlist 2");
        when(mockPlaylistDao.findDistinctPlaylistNamesByUserId("123456")).thenReturn(expectedPlaylists);

        // Act
        List<String> playlists = playlistService.getUserPlaylists("123456");

        // Assert
        assertEquals(2, playlists.size(), "應該返回 2 個播放清單");
        assertEquals("Playlist 1", playlists.get(0));
        assertEquals("Playlist 2", playlists.get(1));
    }

    @Test
    @DisplayName("獲取播放清單歌曲應該返回正確的歌曲列表")
    void shouldReturnPlaylistSongs() {
        // Arrange
        PlaylistItem song1 = new PlaylistItem("123456", "My Playlist", "Test Song", "http://example.com", 180000L, 1);
        List<PlaylistItem> expectedSongs = Arrays.asList(song1);
        when(mockPlaylistDao.findByUserIdAndPlaylistNameOrderBySongOrder("123456", "My Playlist")).thenReturn(expectedSongs);

        // Act
        List<PlaylistItem> songs = playlistService.getPlaylistSongs("123456", "My Playlist");

        // Assert
        assertEquals(1, songs.size(), "應該返回 1 首歌");
        PlaylistItem song = songs.get(0);
        assertEquals("Test Song", song.getSongTitle());
        assertEquals("http://example.com", song.getSongUrl());
        assertEquals(Long.valueOf(180000L), song.getDuration());
    }

    @Test
    @DisplayName("刪除播放清單應該成功")
    void shouldDeletePlaylistSuccessfully() {
        // Arrange
        when(mockPlaylistDao.deleteByUserIdAndPlaylistName("123456", "My Playlist")).thenReturn(true);

        // Act
        boolean result = playlistService.deletePlaylist("123456", "My Playlist");

        // Assert
        assertTrue(result, "刪除播放清單應該成功");
        verify(mockPlaylistDao).deleteByUserIdAndPlaylistName("123456", "My Playlist");
    }
}