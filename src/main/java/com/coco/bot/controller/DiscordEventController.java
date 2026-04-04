package com.coco.bot.controller;

import com.coco.bot.service.MusicService;
import com.coco.bot.service.PlaylistService;
import com.coco.bot.util.CommandParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Discord 事件控制器
 * 處理來自 Discord 的事件和指令
 */
@Controller
public class DiscordEventController extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordEventController.class);

    private final MusicService musicService;
    private final PlaylistService playlistService;

    @Autowired
    public DiscordEventController(MusicService musicService, PlaylistService playlistService) {
        this.musicService = musicService;
        this.playlistService = playlistService;
    }

    /**
     * Discord 訊息接收事件處理器
     * 當頻道中收到訊息時會觸發此方法
     *
     * @param event 訊息接收事件，包含訊息內容、發送者、頻道等資訊
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // 忽略機器人自己發送的訊息，避免無限循環
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        // 使用 CommandParser 解析指令
        CommandParser.ParsedCommand parsedCommand = CommandParser.parseCommand(message);

        // 如果不是有效指令，直接返回
        if (!parsedCommand.isValid()) {
            return;
        }

        String command = parsedCommand.getCommand();
        List<String> args = parsedCommand.getArguments();
        String userId = event.getAuthor().getId();

        logger.debug("收到指令: {} 參數: {} 用戶: {}", command, args, userId);

        // 處理各種指令
        switch (command) {
            case "!play":
                handlePlayCommand(event, args);
                break;
            case "!playlist":
                handlePlaylistCommand(event, args, userId);
                break;
            case "!stop":
                handleStopCommand(event);
                break;
            case "!pause":
                handlePauseCommand(event);
                break;
            case "!resume":
                handleResumeCommand(event);
                break;
            case "!skip":
                handleSkipCommand(event);
                break;
            case "!queue":
                handleQueueCommand(event);
                break;
            case "!help":
                handleHelpCommand(event);
                break;
        }
    }

    /**
     * 處理播放指令
     */
    private void handlePlayCommand(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ 請提供音樂網址！\n使用方法：`!play <網址>`").queue();
            return;
        }

        String url = args.get(0);
        Member member = event.getMember();

        logger.info("用戶 {} 請求播放: {}", event.getAuthor().getName(), url);

        // 檢查用戶是否在語音頻道中
        if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
            VoiceChannel voiceChannel = member.getVoiceState().getChannel().asVoiceChannel();

            // 向用戶顯示載入訊息
            event.getChannel().sendMessage("🔄 正在載入音樂...").queue();

            // 委派給音樂服務處理
            musicService.playMusic(voiceChannel, event.getChannel().asTextChannel(), url);
        } else {
            event.getChannel().sendMessage("❌ 您需要先加入一個語音頻道！").queue();
            logger.warn("用戶 {} 不在語音頻道中", event.getAuthor().getName());
        }
    }

    /**
     * 處理播放清單指令
     */
    private void handlePlaylistCommand(MessageReceivedEvent event, List<String> args, String userId) {
        CommandParser.PlaylistCommand playlistCmd = CommandParser.parsePlaylistCommand(args);
        String subCommand = playlistCmd.getSubCommand();
        List<String> subArgs = playlistCmd.getArguments();

        switch (subCommand.toLowerCase()) {
            case "create":
                playlistService.handleCreatePlaylist(event, subArgs, userId);
                break;
            case "add":
                playlistService.handleAddToPlaylist(event, subArgs, userId);
                break;
            case "list":
                playlistService.handleListPlaylists(event, userId);
                break;
            case "show":
                playlistService.handleShowPlaylist(event, subArgs, userId);
                break;
            case "delete":
                playlistService.handleDeletePlaylist(event, subArgs, userId);
                break;
            case "remove":
                playlistService.handleRemoveFromPlaylist(event, subArgs, userId);
                break;
            case "play":
                playlistService.handlePlayPlaylist(event, subArgs, userId, musicService);
                break;
            default:
                playlistService.handlePlaylistHelp(event);
                break;
        }
    }

    /**
     * 處理停止指令
     */
    private void handleStopCommand(MessageReceivedEvent event) {
        musicService.stopMusic();
        event.getChannel().sendMessage("⏹️ 已停止播放並清空佇列").queue();
    }

    /**
     * 處理暫停指令
     */
    private void handlePauseCommand(MessageReceivedEvent event) {
        musicService.pauseMusic();
        event.getChannel().sendMessage("⏸️ 已暫停播放").queue();
    }

    /**
     * 處理恢復指令
     */
    private void handleResumeCommand(MessageReceivedEvent event) {
        musicService.resumeMusic();
        event.getChannel().sendMessage("▶️ 已恢復播放").queue();
    }

    /**
     * 處理跳過指令
     */
    private void handleSkipCommand(MessageReceivedEvent event) {
        String result = musicService.skipMusic();
        event.getChannel().sendMessage(result).queue();
    }

    /**
     * 處理佇列查詢指令
     */
    private void handleQueueCommand(MessageReceivedEvent event) {
        String queueInfo = musicService.getQueueInfo();
        event.getChannel().sendMessage(queueInfo).queue();
    }

    /**
     * 處理幫助指令
     */
    private void handleHelpCommand(MessageReceivedEvent event) {
        String helpMessage = "🎵 **音樂機器人指令：**\n" +
                "`!play <網址>` - 播放音樂（支援 YouTube、SoundCloud 等）\n" +
                "`!stop` - 停止播放並清空佇列\n" +
                "`!pause` - 暫停播放\n" +
                "`!resume` - 恢復播放\n" +
                "`!skip` - 跳過目前音樂\n" +
                "`!queue` - 查看播放佇列\n" +
                "`!playlist` - 播放清單管理\n" +
                "`!help` - 顯示此幫助訊息\n\n" +
                "🎯 **使用範例：**\n" +
                "`!play https://www.youtube.com/watch?v=dQw4w9WgXcQ`";
        event.getChannel().sendMessage(helpMessage).queue();
    }

}