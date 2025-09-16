package com.coco.bot.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * 音頻播放發送處理器
 *
 * 這個類實現了 JDA 的 AudioSendHandler 介面，用於將 LavaPlayer 的音頻數據
 * 轉換並發送到 Discord 語音頻道。
 *
 * 主要功能：
 * - 從 LavaPlayer 的 AudioPlayer 獲取音頻數據
 * - 將音頻數據格式化為 Discord 可接受的格式
 * - 處理音頻緩衝區管理
 *
 * 技術細節：
 * - 使用 Opus 編碼格式（Discord 的原生音頻格式）
 * - 每次提供 20ms 的音頻數據
 * - 使用 1024 字節的緩衝區來處理音頻數據
 */
public class AudioPlayerSendHandler implements AudioSendHandler {

    /** LavaPlayer 音頻播放器實例 */
    private final AudioPlayer audioPlayer;

    /** 音頻數據緩衝區 */
    private final ByteBuffer buffer;

    /** 可變音頻幀，用於從播放器獲取音頻數據 */
    private final MutableAudioFrame frame;

    /**
     * 建構子
     * 初始化音頻發送處理器
     *
     * @param audioPlayer LavaPlayer 的音頻播放器實例
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;

        // 分配 1024 字節的直接內存緩衝區
        // 這個大小足以容納 20ms 的 Opus 音頻數據
        this.buffer = ByteBuffer.allocate(1024);

        // 創建可變音頻幀並設置緩衝區
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * 檢查是否可以提供音頻數據
     *
     * 這個方法會被 JDA 定期調用，用於確定是否有音頻數據可以發送
     * 同時會從 AudioPlayer 獲取下一幀音頻數據到緩衝區中
     *
     * @return true 如果有音頻數據可以提供，false 如果沒有
     */
    @Override
    public boolean canProvide() {
        // 嘗試從音頻播放器獲取下一幀數據
        // 如果成功獲取，返回 true；如果沒有數據（如播放器停止），返回 false
        return audioPlayer.provide(frame);
    }

    /**
     * 提供 20 毫秒的音頻數據
     *
     * 當 canProvide() 返回 true 時，JDA 會調用這個方法來獲取實際的音頻數據
     * 這些數據將被發送到 Discord 語音頻道
     *
     * @return 包含 20ms 音頻數據的 ByteBuffer
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        // flip() 操作將緩衝區從寫入模式切換到讀取模式
        // 這會設置 limit 為當前 position，並將 position 重置為 0
        buffer.flip();
        return buffer;
    }

    /**
     * 指示音頻格式是否為 Opus
     *
     * Discord 原生支援 Opus 編碼，使用 Opus 可以減少 CPU 使用量
     * 因為不需要額外的編碼步驟
     *
     * @return true 表示音頻數據已經是 Opus 格式
     */
    @Override
    public boolean isOpus() {
        return true;
    }
}