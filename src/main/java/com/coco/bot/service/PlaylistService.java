package com.coco.bot.service;

import com.coco.bot.dao.PlaylistDao;
import com.coco.bot.entity.PlaylistItem;
import com.coco.bot.handler.YouTubeResolver;
import com.coco.bot.util.CommandParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 播放清單服務類
 * 使用 DAO 層進行數據庫操作並處理 Discord 事件
 */
@Service
@Transactional
public class PlaylistService {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistDao playlistDao;
    private final YouTubeResolver youTubeResolver;

    @Autowired
    public PlaylistService(PlaylistDao playlistDao, YouTubeResolver youTubeResolver) {
        this.playlistDao = playlistDao;
        this.youTubeResolver = youTubeResolver;
    }

    /**
     * 創建播放清單（添加第一首歌）
     */
    public boolean createPlaylist(String userId, String playlistName, String songTitle, String songUrl, Long duration) {
        try {
            PlaylistItem item = new PlaylistItem(userId, playlistName, songTitle, songUrl, duration != null ? duration : 0, 1);
            return playlistDao.save(item);
        } catch (Exception e) {
            logger.error("創建播放清單失敗: userId={}, playlistName={}", userId, playlistName, e);
            return false;
        }
    }

    /**
     * 添加歌曲到現有播放清單
     */
    public boolean addSongToPlaylist(String userId, String playlistName, String songTitle, String songUrl, Long duration) {
        try {
            Integer maxOrder = playlistDao.findMaxSongOrderByUserIdAndPlaylistName(userId, playlistName);

            // 如果是第一首真正的歌曲（maxOrder為0），則從1開始
            Integer nextOrder = (maxOrder == 0) ? 1 : maxOrder + 1;

            PlaylistItem item = new PlaylistItem(userId, playlistName, songTitle, songUrl, duration != null ? duration : 0, nextOrder);
            return playlistDao.save(item);
        } catch (Exception e) {
            logger.error("添加歌曲到播放清單失敗: userId={}, playlistName={}", userId, playlistName, e);
            return false;
        }
    }

    /**
     * 獲取用戶的所有播放清單名稱
     */
    public List<String> getUserPlaylists(String userId) {
        return playlistDao.findDistinctPlaylistNamesByUserId(userId);
    }

    /**
     * 獲取播放清單的所有歌曲
     */
    public List<PlaylistItem> getPlaylistSongs(String userId, String playlistName) {
        List<PlaylistItem> allItems = playlistDao.findByUserIdAndPlaylistNameOrderBySongOrder(userId, playlistName);
        // 過濾掉空的播放清單標記項目（songTitle和songUrl都為空的項目）
        return allItems.stream()
                .filter(item -> item.getSongTitle() != null && !item.getSongTitle().isEmpty()
                            && item.getSongUrl() != null && !item.getSongUrl().isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 刪除整個播放清單
     */
    public boolean deletePlaylist(String userId, String playlistName) {
        return playlistDao.deleteByUserIdAndPlaylistName(userId, playlistName);
    }

    /**
     * 從播放清單中移除特定歌曲
     */
    public boolean removeSongFromPlaylist(String userId, String playlistName, int songOrder) {
        boolean deleted = playlistDao.deleteBySongOrder(userId, playlistName, songOrder);
        if (deleted) {
            playlistDao.reorderSongsAfterDeletion(userId, playlistName, songOrder);
        }
        return deleted;
    }

    /**
     * 檢查播放清單是否存在
     */
    public boolean playlistExists(String userId, String playlistName) {
        return playlistDao.existsByUserIdAndPlaylistName(userId, playlistName);
    }

    // Discord 事件處理方法

    /**
     * 處理創建播放清單指令
     */
    public void handleCreatePlaylist(MessageReceivedEvent event, List<String> args, String userId) {
        if (args.size() < 1) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist create <播放清單名稱> [歌曲網址]`").queue();
            return;
        }

        String playlistName = args.get(0);

        if (playlistExists(userId, playlistName)) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 已存在！").queue();
            return;
        }

        // 檢查是否有提供歌曲網址
        if (args.size() >= 2) {
            String songUrl = args.get(1);
            loadSongInfoAndCreatePlaylist(event, userId, playlistName, songUrl);
        } else {
            // 創建空的播放清單
            createEmptyPlaylist(event, userId, playlistName);
        }
    }

    /**
     * 處理添加歌曲到播放清單指令
     */
    public void handleAddToPlaylist(MessageReceivedEvent event, List<String> args, String userId) {
        if (args.size() < 2) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist add <播放清單名稱> <歌曲網址>`").queue();
            return;
        }

        String playlistName = args.get(0);
        String songUrl = args.get(1);

        if (!playlistExists(userId, playlistName)) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 不存在！\n使用 `!playlist create " + playlistName + " <網址>` 來創建。").queue();
            return;
        }

        loadSongInfoAndAddToPlaylist(event, userId, playlistName, songUrl);
    }

    /**
     * 處理列出播放清單指令
     */
    public void handleListPlaylists(MessageReceivedEvent event, String userId) {
        List<String> playlists = getUserPlaylists(userId);

        if (playlists.isEmpty()) {
            event.getChannel().sendMessage("📝 你還沒有創建任何播放清單。\n使用 `!playlist create <名稱> <網址>` 來創建第一個播放清單！").queue();
            return;
        }

        StringBuilder message = new StringBuilder("🎵 **你的播放清單：**\n");
        for (int i = 0; i < playlists.size(); i++) {
            String playlistName = playlists.get(i);
            List<PlaylistItem> songs = getPlaylistSongs(userId, playlistName);
            message.append(String.format("%d. **%s** (%d 首歌)\n", i + 1, playlistName, songs.size()));
        }
        message.append("\n使用 `!playlist show <名稱>` 查看播放清單內容");

        event.getChannel().sendMessage(message.toString()).queue();
    }

    /**
     * 處理顯示播放清單內容指令
     */
    public void handleShowPlaylist(MessageReceivedEvent event, List<String> args, String userId) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist show <播放清單名稱>`").queue();
            return;
        }

        String playlistName = args.get(0);
        List<PlaylistItem> songs = getPlaylistSongs(userId, playlistName);

        if (songs.isEmpty()) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 不存在或為空！").queue();
            return;
        }

        StringBuilder message = new StringBuilder("🎵 **播放清單：" + playlistName + "**\n");
        for (PlaylistItem song : songs) {
            message.append(String.format("%d. **%s** (%s)\n",
                    song.getSongOrder(),
                    song.getSongTitle(),
                    CommandParser.formatDuration(song.getDuration())));
        }
        message.append("\n使用 `!playlist play ").append(playlistName).append("` 播放整個清單");

        event.getChannel().sendMessage(message.toString()).queue();
    }

    /**
     * 處理刪除播放清單指令
     */
    public void handleDeletePlaylist(MessageReceivedEvent event, List<String> args, String userId) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist delete <播放清單名稱>`").queue();
            return;
        }

        String playlistName = args.get(0);

        if (!playlistExists(userId, playlistName)) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 不存在！").queue();
            return;
        }

        boolean success = deletePlaylist(userId, playlistName);
        if (success) {
            event.getChannel().sendMessage("✅ 播放清單 **" + playlistName + "** 已刪除！").queue();
        } else {
            event.getChannel().sendMessage("❌ 刪除播放清單時發生錯誤，請稍後再試。").queue();
        }
    }

    /**
     * 處理從播放清單移除歌曲指令
     */
    public void handleRemoveFromPlaylist(MessageReceivedEvent event, List<String> args, String userId) {
        if (args.size() < 2) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist remove <播放清單名稱> <歌曲序號>`").queue();
            return;
        }

        String playlistName = args.get(0);
        int songOrder;

        try {
            songOrder = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ 歌曲序號必須是數字！").queue();
            return;
        }

        if (!playlistExists(userId, playlistName)) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 不存在！").queue();
            return;
        }

        boolean success = removeSongFromPlaylist(userId, playlistName, songOrder);
        if (success) {
            event.getChannel().sendMessage("✅ 已從播放清單 **" + playlistName + "** 移除第 " + songOrder + " 首歌！").queue();
        } else {
            event.getChannel().sendMessage("❌ 移除歌曲失敗，請檢查歌曲序號是否正確。").queue();
        }
    }

    /**
     * 處理播放播放清單指令
     */
    public void handlePlayPlaylist(MessageReceivedEvent event, List<String> args, String userId, MusicService musicService) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ 使用方法：`!playlist play <播放清單名稱>`").queue();
            return;
        }

        String playlistName = args.get(0);
        List<PlaylistItem> songs = getPlaylistSongs(userId, playlistName);

        if (songs.isEmpty()) {
            event.getChannel().sendMessage("❌ 播放清單 **" + playlistName + "** 不存在或為空！").queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage("❌ 您需要先加入一個語音頻道！").queue();
            return;
        }

        VoiceChannel voiceChannel = member.getVoiceState().getChannel().asVoiceChannel();
        event.getChannel().sendMessage("🎵 **開始播放播放清單：" + playlistName + "** (" + songs.size() + " 首歌)").queue();

        // 播放第一首歌並將其餘歌曲加入佇列
        for (int i = 0; i < songs.size(); i++) {
            PlaylistItem song = songs.get(i);
            if (i == 0) {
                musicService.playMusic(voiceChannel, event.getChannel().asTextChannel(), song.getSongUrl());
            } else {
                // 這裡需要添加到佇列的方法
                // musicService.addToQueue(song.getSongUrl());
            }
        }
    }

    /**
     * 處理播放清單幫助指令
     */
    public void handlePlaylistHelp(MessageReceivedEvent event) {
        String helpMessage = "🎵 **播放清單指令：**\n" +
                "`!playlist create <名稱> [網址]` - 創建新的播放清單（網址可選）\n" +
                "`!playlist add <名稱> <網址>` - 添加歌曲到播放清單\n" +
                "`!playlist list` - 列出你的所有播放清單\n" +
                "`!playlist show <名稱>` - 查看播放清單內容\n" +
                "`!playlist play <名稱>` - 播放整個播放清單\n" +
                "`!playlist remove <名稱> <序號>` - 移除播放清單中的歌曲\n" +
                "`!playlist delete <名稱>` - 刪除播放清單\n\n" +
                "📝 **範例：**\n" +
                "`!playlist create 我的最愛` - 創建空播放清單\n" +
                "`!playlist create 我的最愛 https://www.youtube.com/watch?v=dQw4w9WgXcQ` - 創建並添加歌曲\n" +
                "`!playlist add 我的最愛 https://www.youtube.com/watch?v=fJ9rUzIMcZQ`";

        event.getChannel().sendMessage(helpMessage).queue();
    }

    /**
     * 載入歌曲信息並創建播放清單
     */
    private void loadSongInfoAndCreatePlaylist(MessageReceivedEvent event, String userId, String playlistName, String songUrl) {
        new Thread(() -> {
            try {
                if (songUrl.contains("youtube.com") || songUrl.contains("youtu.be")) {
                    YouTubeResolver.TrackInfo trackInfo = youTubeResolver.resolveYouTubeUrl(songUrl);
                    if (trackInfo != null) {
                        boolean success = createPlaylist(userId, playlistName, trackInfo.title, trackInfo.url, trackInfo.duration);
                        if (success) {
                            event.getChannel().sendMessage("✅ 播放清單 **" + playlistName + "** 創建成功！\n🎵 已添加：**" + trackInfo.title + "**").queue();
                        } else {
                            event.getChannel().sendMessage("❌ 創建播放清單失敗，請稍後再試。").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("❌ 無法解析該 YouTube 影片，請檢查網址。").queue();
                    }
                } else {
                    boolean success = createPlaylist(userId, playlistName, "Unknown Title", songUrl, 0L);
                    if (success) {
                        event.getChannel().sendMessage("✅ 播放清單 **" + playlistName + "** 創建成功！\n🎵 已添加歌曲").queue();
                    } else {
                        event.getChannel().sendMessage("❌ 創建播放清單失敗，請稍後再試。").queue();
                    }
                }
            } catch (Exception e) {
                logger.error("創建播放清單時發生錯誤", e);
                event.getChannel().sendMessage("❌ 創建播放清單時發生錯誤，請稍後再試。").queue();
            }
        }).start();
    }

    /**
     * 載入歌曲信息並添加到播放清單
     */
    private void loadSongInfoAndAddToPlaylist(MessageReceivedEvent event, String userId, String playlistName, String songUrl) {
        new Thread(() -> {
            try {
                if (songUrl.contains("youtube.com") || songUrl.contains("youtu.be")) {
                    YouTubeResolver.TrackInfo trackInfo = youTubeResolver.resolveYouTubeUrl(songUrl);
                    if (trackInfo != null) {
                        boolean success = addSongToPlaylist(userId, playlistName, trackInfo.title, trackInfo.url, trackInfo.duration);
                        if (success) {
                            event.getChannel().sendMessage("✅ 已添加到播放清單 **" + playlistName + "**：\n🎵 **" + trackInfo.title + "**").queue();
                        } else {
                            event.getChannel().sendMessage("❌ 添加歌曲失敗，請稍後再試。").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("❌ 無法解析該 YouTube 影片，請檢查網址。").queue();
                    }
                } else {
                    boolean success = addSongToPlaylist(userId, playlistName, "Unknown Title", songUrl, 0L);
                    if (success) {
                        event.getChannel().sendMessage("✅ 已添加歌曲到播放清單 **" + playlistName + "**").queue();
                    } else {
                        event.getChannel().sendMessage("❌ 添加歌曲失敗，請稍後再試。").queue();
                    }
                }
            } catch (Exception e) {
                logger.error("添加歌曲到播放清單時發生錯誤", e);
                event.getChannel().sendMessage("❌ 添加歌曲時發生錯誤，請稍後再試。").queue();
            }
        }).start();
    }

    /**
     * 創建空的播放清單
     */
    private void createEmptyPlaylist(MessageReceivedEvent event, String userId, String playlistName) {
        try {
            // 創建一個空的播放清單項目作為標記
            PlaylistItem emptyItem = new PlaylistItem(userId, playlistName, "", "", 0L, 0);
            boolean success = playlistDao.save(emptyItem);

            if (success) {
                event.getChannel().sendMessage("✅ 空播放清單 **" + playlistName + "** 創建成功！\n" +
                    "使用 `!playlist add " + playlistName + " <網址>` 來添加歌曲。").queue();
            } else {
                event.getChannel().sendMessage("❌ 創建播放清單失敗，請稍後再試。").queue();
            }
        } catch (Exception e) {
            logger.error("創建空播放清單時發生錯誤", e);
            event.getChannel().sendMessage("❌ 創建播放清單時發生錯誤，請稍後再試。").queue();
        }
    }
}