package com.coco.bot.handler;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 音樂佇列管理系統
 * 負責管理播放佇列，支援佇列播放和跳過功能
 */
@Component
public class MusicQueue {

    private final Queue<AudioTrack> queue;
    private AudioTrack currentTrack;

    public MusicQueue() {
        this.queue = new LinkedList<>();
        this.currentTrack = null;
    }

    public void addTrack(AudioTrack track) {
        queue.offer(track);
    }

    public AudioTrack getNextTrack() {
        currentTrack = queue.poll();
        return currentTrack;
    }

    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(AudioTrack track) {
        this.currentTrack = track;
    }

    public AudioTrack skipCurrentTrack() {
        return getNextTrack();
    }

    public void clearQueue() {
        queue.clear();
        currentTrack = null;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean hasCurrentTrack() {
        return currentTrack != null;
    }
}
