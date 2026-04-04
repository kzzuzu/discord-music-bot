package com.coco.bot.service;

import com.coco.bot.handler.AudioPlayerSendHandler;
import com.coco.bot.handler.MusicQueue;
import com.coco.bot.handler.YouTubeResolver;
import com.coco.bot.util.CommandParser;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 音樂服務類
 * 處理音樂播放相關的業務邏輯
 */
@Service
public class MusicService {
    private static final Logger logger = LoggerFactory.getLogger(MusicService.class);

    private final AudioPlayerManager audioPlayerManager;
    private final AudioPlayer audioPlayer;
    private final AudioPlayerSendHandler sendHandler;
    private final MusicQueue musicQueue;
    private final YouTubeResolver youTubeResolver;

    @Autowired
    public MusicService(MusicQueue musicQueue, YouTubeResolver youTubeResolver) {
        this.musicQueue = musicQueue;
        this.youTubeResolver = youTubeResolver;

        // 創建預設的音頻播放管理器
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        // 配置 YouTube 來源管理器
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);

        // 註冊增強型 YouTube 來源管理器
        try {
            // Web：負責 metadata loading（loadVideo）
            // Tv：唯一支援 OAuth2 的 client，負責 stream URL 取得（loadFormats）
            // 內建 JS cipher 提取器在 1.18.0 已失效，改用遠端 cipher server
            dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager =
                new dev.lavalink.youtube.YoutubeAudioSourceManager(
                    new dev.lavalink.youtube.clients.Web(),
                    new dev.lavalink.youtube.clients.Tv()
                );

            // 使用遠端 cipher server 解決 YouTube 更換 player 腳本問題
            ytSourceManager.setCipherManager(
                new dev.lavalink.youtube.cipher.RemoteCipherManager("https://cipher.kikkia.dev/")
            );
            logger.info("已啟用遠端 cipher server");

            ytSourceManager.useOauth2(null, false);
            logger.info("YouTube OAuth2 已啟用（token 快取於 oauth2.json）");

            audioPlayerManager.registerSourceManager(ytSourceManager);
            logger.info("成功註冊增強型 YouTube 來源管理器 (dev.lavalink.youtube)");
        } catch (Exception e) {
            logger.error("無法註冊 YouTube 來源管理器: {}", e.getMessage(), e);
            // 使用預設來源管理器作為備用
            AudioSourceManagers.registerRemoteSources(audioPlayerManager);
            logger.info("使用預設來源管理器");
        }

        AudioSourceManagers.registerLocalSource(audioPlayerManager);

        // 創建音頻播放器實例
        this.audioPlayer = audioPlayerManager.createPlayer();

        // 創建音頻發送處理器
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);

        // 註冊音頻事件監聽器
        audioPlayer.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                logger.info("音軌結束: {} 原因: {}", track.getInfo().title, endReason);
                if (endReason.mayStartNext) {
                    AudioTrack nextTrack = musicQueue.getNextTrack();
                    if (nextTrack != null) {
                        player.playTrack(nextTrack);
                        logger.info("自動播放下一首: {}", nextTrack.getInfo().title);
                    } else {
                        logger.info("佇列已空，播放結束");
                    }
                }
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                logger.error("音軌播放異常: {} 錯誤: {}", track.getInfo().title, exception.getMessage(), exception);
            }

            @Override
            public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
                logger.warn("音軌卡住: {} 超過 {}ms", track.getInfo().title, thresholdMs);
            }
        });

        logger.info("音樂服務初始化完成");
    }

    /**
     * 播放音樂
     *
     * @param voiceChannel 語音頻道
     * @param textChannel 文字頻道
     * @param url 音樂網址
     */
    public void playMusic(VoiceChannel voiceChannel, TextChannel textChannel, String url) {
        connectToVoiceChannel(voiceChannel.getGuild().getAudioManager(), voiceChannel);
        loadAndPlay(textChannel, url);
    }

    /**
     * 將音樂加入佇列（不重新連接語音頻道）
     */
    public void queueMusic(TextChannel textChannel, String url) {
        loadAndPlay(textChannel, url);
    }

    /**
     * 停止音樂播放
     */
    public void stopMusic() {
        audioPlayer.stopTrack();
        musicQueue.clearQueue();
    }

    /**
     * 暫停音樂播放
     */
    public void pauseMusic() {
        audioPlayer.setPaused(true);
    }

    /**
     * 恢復音樂播放
     */
    public void resumeMusic() {
        audioPlayer.setPaused(false);
    }

    /**
     * 跳過當前音樂
     *
     * @return 跳過結果訊息
     */
    public String skipMusic() {
        AudioTrack currentTrack = musicQueue.getCurrentTrack();
        if (currentTrack != null) {
            String currentTitle = currentTrack.getInfo().title;
            AudioTrack nextTrack = musicQueue.skipCurrentTrack();

            if (nextTrack != null) {
                audioPlayer.playTrack(nextTrack);
                logger.info("跳過音軌: {} -> {}", currentTitle, nextTrack.getInfo().title);
                return "⏭️ 已跳過: **" + currentTitle + "**\n🎵 正在播放: **" + nextTrack.getInfo().title + "**";
            } else {
                audioPlayer.stopTrack();
                logger.info("跳過音軌: {}，佇列已空", currentTitle);
                return "⏭️ 已跳過: **" + currentTitle + "**\n佇列已空，播放結束";
            }
        } else {
            return "❌ 目前沒有正在播放的音樂";
        }
    }

    /**
     * 獲取佇列資訊
     *
     * @return 佇列資訊字串
     */
    public String getQueueInfo() {
        StringBuilder queueInfo = new StringBuilder();
        queueInfo.append("🎵 **播放佇列:**\n");

        AudioTrack current = musicQueue.getCurrentTrack();
        if (current != null) {
            queueInfo.append("🔄 **目前播放:** ").append(current.getInfo().title)
                    .append(" (").append(CommandParser.formatDuration(current.getDuration())).append(")\n");
        } else {
            queueInfo.append("🔄 **目前播放:** 無\n");
        }

        if (musicQueue.isEmpty()) {
            queueInfo.append("📜 **佇列:** 空的");
        } else {
            queueInfo.append("📜 **佇列 (").append(musicQueue.getQueueSize()).append(" 首):**\n");
            queueInfo.append("ℹ️ 佇列中有 ").append(musicQueue.getQueueSize()).append(" 首音樂等待播放");
        }

        return queueInfo.toString();
    }

    /**
     * 連接到語音頻道
     */
    private void connectToVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
        audioManager.setSelfDeafened(false);
        audioManager.setSelfMuted(false);
        audioManager.setSendingHandler(sendHandler);
        audioManager.openAudioConnection(voiceChannel);
        logger.info("語音連線已請求: {}", voiceChannel.getName());
    }

    /**
     * 載入並播放音樂
     * 現在直接使用 LavaPlayer 的 YouTube 來源管理器，不再需要 yt-dlp 解析
     */
    private void loadAndPlay(TextChannel channel, String trackUrl) {
        audioPlayerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                handleTrackLoaded(channel, track, track.getInfo().title, track.getDuration());
                logger.info("✅ 成功載入音軌: {}", track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null && !playlist.getTracks().isEmpty()) {
                    firstTrack = playlist.getTracks().get(0);
                }
                if (firstTrack != null) {
                    handleTrackLoaded(channel, firstTrack, firstTrack.getInfo().title, firstTrack.getDuration());
                    logger.info("✅ 成功載入播放清單首曲: {}", firstTrack.getInfo().title);
                } else {
                    channel.sendMessage("❌ 播放清單為空。").queue();
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("❌ 找不到該音樂。請檢查網址是否正確。").queue();
                logger.warn("❌ 無法找到匹配的音軌: {}", trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("❌ 載入音樂時發生錯誤: " + exception.getMessage()).queue();
                logger.error("❌ 載入音軌失敗: {} - {}", trackUrl, exception.getMessage(), exception);
            }
        });
    }

    /**
     * 處理音軌載入完成
     */
    private void handleTrackLoaded(TextChannel channel, AudioTrack track, String title, long duration) {
        if (audioPlayer.getPlayingTrack() == null) {
            musicQueue.setCurrentTrack(track);
            audioPlayer.playTrack(track);
            channel.sendMessage("🎵 **正在播放:** " + title +
                    " (" + CommandParser.formatDuration(duration) + ")").queue();
            logger.info("開始播放音軌: {}", title);
        } else {
            musicQueue.addTrack(track);
            channel.sendMessage("📝 **已加入佇列:** " + title +
                    " (" + CommandParser.formatDuration(duration) + ")" +
                    "\n🔢 **佇列位置:** " + musicQueue.getQueueSize()).queue();
            logger.info("音軌已加入佇列: {} (位置: {})", title, musicQueue.getQueueSize());
        }
    }

}