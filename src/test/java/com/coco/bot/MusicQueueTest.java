package com.coco.bot;

import com.coco.bot.handler.MusicQueue;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MusicQueue 類別的單元測試
 */
class MusicQueueTest {

    private MusicQueue musicQueue;
    
    @Mock
    private AudioTrack mockTrack1;
    
    @Mock
    private AudioTrack mockTrack2;
    
    @Mock
    private AudioTrack mockTrack3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        musicQueue = new MusicQueue();
        
        // 設定 mock 物件
        AudioTrackInfo trackInfo1 = new AudioTrackInfo("Test Song 1", "Test Artist", 180000, "test-id-1", false, "test-uri-1");
        AudioTrackInfo trackInfo2 = new AudioTrackInfo("Test Song 2", "Test Artist", 240000, "test-id-2", false, "test-uri-2");
        AudioTrackInfo trackInfo3 = new AudioTrackInfo("Test Song 3", "Test Artist", 200000, "test-id-3", false, "test-uri-3");
        
        when(mockTrack1.getInfo()).thenReturn(trackInfo1);
        when(mockTrack2.getInfo()).thenReturn(trackInfo2);
        when(mockTrack3.getInfo()).thenReturn(trackInfo3);
    }

    @Test
    @DisplayName("新建的佇列應該是空的")
    void newQueueShouldBeEmpty() {
        assertTrue(musicQueue.isEmpty(), "新建的佇列應該是空的");
        assertEquals(0, musicQueue.getQueueSize(), "新建的佇列大小應該是 0");
        assertFalse(musicQueue.hasCurrentTrack(), "新建的佇列不應該有目前播放的音軌");
        assertNull(musicQueue.getCurrentTrack(), "新建的佇列的目前音軌應該是 null");
    }

    @Test
    @DisplayName("應該能夠加入音軌到佇列")
    void shouldAddTrackToQueue() {
        musicQueue.addTrack(mockTrack1);
        
        assertFalse(musicQueue.isEmpty(), "加入音軌後佇列不應該是空的");
        assertEquals(1, musicQueue.getQueueSize(), "加入一首音軌後佇列大小應該是 1");
    }

    @Test
    @DisplayName("應該能夠獲取下一首音軌")
    void shouldGetNextTrack() {
        musicQueue.addTrack(mockTrack1);
        musicQueue.addTrack(mockTrack2);
        
        AudioTrack nextTrack = musicQueue.getNextTrack();
        
        assertEquals(mockTrack1, nextTrack, "應該獲取第一首加入的音軌");
        assertEquals(mockTrack1, musicQueue.getCurrentTrack(), "目前音軌應該被設定為獲取的音軌");
        assertEquals(1, musicQueue.getQueueSize(), "獲取音軌後佇列大小應該減少");
    }

    @Test
    @DisplayName("佇列為空時獲取下一首音軌應該返回 null")
    void shouldReturnNullWhenGettingNextTrackFromEmptyQueue() {
        AudioTrack nextTrack = musicQueue.getNextTrack();
        
        assertNull(nextTrack, "空佇列應該返回 null");
        assertNull(musicQueue.getCurrentTrack(), "目前音軌應該保持 null");
    }

    @Test
    @DisplayName("應該能夠設定目前播放的音軌")
    void shouldSetCurrentTrack() {
        musicQueue.setCurrentTrack(mockTrack1);
        
        assertEquals(mockTrack1, musicQueue.getCurrentTrack(), "目前音軌應該被正確設定");
        assertTrue(musicQueue.hasCurrentTrack(), "應該有目前播放的音軌");
    }

    @Test
    @DisplayName("應該能夠跳過目前音軌")
    void shouldSkipCurrentTrack() {
        musicQueue.setCurrentTrack(mockTrack1);
        musicQueue.addTrack(mockTrack2);
        
        AudioTrack nextTrack = musicQueue.skipCurrentTrack();
        
        assertEquals(mockTrack2, nextTrack, "跳過後應該獲取下一首音軌");
        assertEquals(mockTrack2, musicQueue.getCurrentTrack(), "目前音軌應該更新為下一首");
    }

    @Test
    @DisplayName("沒有下一首音軌時跳過應該返回 null")
    void shouldReturnNullWhenSkippingWithNoNextTrack() {
        musicQueue.setCurrentTrack(mockTrack1);
        
        AudioTrack nextTrack = musicQueue.skipCurrentTrack();
        
        assertNull(nextTrack, "沒有下一首音軌時應該返回 null");
        assertNull(musicQueue.getCurrentTrack(), "目前音軌應該被清除");
    }

    @Test
    @DisplayName("應該能夠清空佇列")
    void shouldClearQueue() {
        musicQueue.addTrack(mockTrack1);
        musicQueue.addTrack(mockTrack2);
        musicQueue.setCurrentTrack(mockTrack3);
        
        musicQueue.clearQueue();
        
        assertTrue(musicQueue.isEmpty(), "清空後佇列應該是空的");
        assertEquals(0, musicQueue.getQueueSize(), "清空後佇列大小應該是 0");
        assertNull(musicQueue.getCurrentTrack(), "清空後目前音軌應該是 null");
        assertFalse(musicQueue.hasCurrentTrack(), "清空後不應該有目前播放的音軌");
    }

    @Test
    @DisplayName("應該正確報告佇列大小")
    void shouldReportCorrectQueueSize() {
        assertEquals(0, musicQueue.getQueueSize());
        
        musicQueue.addTrack(mockTrack1);
        assertEquals(1, musicQueue.getQueueSize());
        
        musicQueue.addTrack(mockTrack2);
        assertEquals(2, musicQueue.getQueueSize());
        
        musicQueue.getNextTrack();
        assertEquals(1, musicQueue.getQueueSize());
        
        musicQueue.getNextTrack();
        assertEquals(0, musicQueue.getQueueSize());
    }
}