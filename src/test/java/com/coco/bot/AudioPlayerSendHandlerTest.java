package com.coco.bot;

import com.coco.bot.handler.AudioPlayerSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AudioPlayerSendHandler 類別的單元測試
 */
class AudioPlayerSendHandlerTest {

    private AudioPlayerSendHandler sendHandler;
    
    @Mock
    private AudioPlayer mockAudioPlayer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sendHandler = new AudioPlayerSendHandler(mockAudioPlayer);
    }

    @Test
    @DisplayName("建構子應該正確初始化")
    void constructorShouldInitializeCorrectly() {
        assertNotNull(sendHandler, "AudioPlayerSendHandler 不應為 null");
    }

    @Test
    @DisplayName("isOpus 應該返回 true")
    void isOpusShouldReturnTrue() {
        assertTrue(sendHandler.isOpus(), "isOpus 應該返回 true，因為我們使用 Opus 格式");
    }

    @Test
    @DisplayName("當音頻播放器有數據時 canProvide 應該返回 true")
    void canProvideShouldReturnTrueWhenPlayerHasData() {
        when(mockAudioPlayer.provide(any(MutableAudioFrame.class))).thenReturn(true);
        
        boolean result = sendHandler.canProvide();
        
        assertTrue(result, "當播放器有數據時應該返回 true");
        verify(mockAudioPlayer).provide(any(MutableAudioFrame.class));
    }

    @Test
    @DisplayName("當音頻播放器沒有數據時 canProvide 應該返回 false")
    void canProvideShouldReturnFalseWhenPlayerHasNoData() {
        when(mockAudioPlayer.provide(any(MutableAudioFrame.class))).thenReturn(false);
        
        boolean result = sendHandler.canProvide();
        
        assertFalse(result, "當播放器沒有數據時應該返回 false");
        verify(mockAudioPlayer).provide(any(MutableAudioFrame.class));
    }

    @Test
    @DisplayName("provide20MsAudio 應該返回 ByteBuffer")
    void provide20MsAudioShouldReturnByteBuffer() {
        // 先調用 canProvide 來準備數據
        when(mockAudioPlayer.provide(any(MutableAudioFrame.class))).thenReturn(true);
        sendHandler.canProvide();
        
        ByteBuffer result = sendHandler.provide20MsAudio();
        
        assertNotNull(result, "provide20MsAudio 應該返回非 null 的 ByteBuffer");
        assertTrue(result.capacity() > 0, "ByteBuffer 應該有正的容量");
    }

    @Test
    @DisplayName("多次調用 canProvide 應該每次都調用播放器的 provide 方法")
    void multipleCanProvideCallsShouldCallPlayerProvideEachTime() {
        when(mockAudioPlayer.provide(any(MutableAudioFrame.class))).thenReturn(true, false, true);
        
        boolean result1 = sendHandler.canProvide();
        boolean result2 = sendHandler.canProvide();
        boolean result3 = sendHandler.canProvide();
        
        assertTrue(result1, "第一次調用應該返回 true");
        assertFalse(result2, "第二次調用應該返回 false");
        assertTrue(result3, "第三次調用應該返回 true");
        
        verify(mockAudioPlayer, times(3)).provide(any(MutableAudioFrame.class));
    }
}