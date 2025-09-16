package com.coco.bot.handler;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 音樂佇列管理系統
 * 負責管理播放佇列，支援佇列播放和跳過功能
 */
@Component
public class MusicQueue {
    private static final Logger logger = LoggerFactory.getLogger(MusicQueue.class);

    private final Queue<AudioTrack> queue;
    private AudioTrack currentTrack;

    public MusicQueue() {
        this.queue = new LinkedList<>();
        this.currentTrack = null;
    }

    /**
     * 將音軌加入佇列
     *
     * @param track 要加入的音軌
     */
    public void addTrack(AudioTrack track) {
        queue.offer(track);
        logger.info("音軌已加入佇列: {} (佇列大小: {})", track.getInfo().title, queue.size());
    }

    /**
     * 獲取下一首音軌
     *
     * @return 下一首音軌，如果佇列為空則返回 null
     */
    public AudioTrack getNextTrack() {
        AudioTrack nextTrack = queue.poll();
        if (nextTrack != null) {
            currentTrack = nextTrack;
            logger.info("開始播放下一首音軌: {}", nextTrack.getInfo().title);
        } else {
            currentTrack = null;
            logger.info("佇列已空");
        }
        return nextTrack;
    }

    /**
     * 獲取目前正在播放的音軌
     *
     * @return 目前播放的音軌
     */
    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    /**
     * 設定目前播放的音軌
     *
     * @param track 要設定的音軌
     */
    public void setCurrentTrack(AudioTrack track) {
        this.currentTrack = track;
        if (track != null) {
            logger.info("設定目前播放音軌: {}", track.getInfo().title);
        }
    }

    /**
     * 跳過目前音軌，播放下一首
     *
     * @return 下一首音軌，如果佇列為空則返回 null
     */
    public AudioTrack skipCurrentTrack() {
        if (currentTrack != null) {
            logger.info("跳過目前音軌: {}", currentTrack.getInfo().title);
        }
        return getNextTrack();
    }

    /**
     * 清空佇列
     */
    public void clearQueue() {
        queue.clear();
        currentTrack = null;
        logger.info("佇列已清空");
    }

    /**
     * 獲取佇列大小
     *
     * @return 佇列中的音軌數量
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * 檢查佇列是否為空
     *
     * @return true 如果佇列為空
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * 檢查是否有音軌正在播放
     *
     * @return true 如果有音軌正在播放
     */
    public boolean hasCurrentTrack() {
        return currentTrack != null;
    }
}