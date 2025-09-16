package com.coco.bot.service;

import com.coco.bot.controller.DiscordEventController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

@Service
public class DiscordBotService {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBotService.class);

    @Value("${discord.bot.token}")
    private String botToken;

    private final DiscordEventController discordEventController;
    private JDA jda;

    @Autowired
    public DiscordBotService(DiscordEventController discordEventController) {
        this.discordEventController = discordEventController;
    }

    public void startBot() {
        try {
            logger.info("正在啟動 Discord 音樂機器人...");

            JDABuilder builder = JDABuilder.createDefault(botToken);
            builder.addEventListeners(discordEventController);
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES);

            this.jda = builder.build();
            this.jda.awaitReady();

            // 添加關閉鉤子，確保程式結束時機器人正確關閉
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("正在關閉 Discord 機器人...");
                shutdown();
            }));

            logger.info("🎵 Discord 音樂機器人啟動成功！");
            logger.info("支援指令：!play, !stop, !pause, !resume, !skip, !queue, !playlist, !help");
        } catch (Exception e) {
            logger.error("機器人啟動失敗", e);
            throw new RuntimeException("Failed to start Discord bot", e);
        }
    }

    /**
     * 強制關閉機器人
     * 當應用程式關閉時會自動調用此方法
     */
    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            logger.info("正在強制關閉 Discord 連接...");
            try {
                // 立即強制關閉，不等待
                jda.shutdownNow();
                // 只等待 3 秒確認關閉
                if (jda.awaitShutdown(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    logger.info("✅ Discord 機器人已成功關閉");
                } else {
                    logger.warn("⚠️ 機器人可能未完全關閉，但已強制終止");
                }
            } catch (InterruptedException e) {
                logger.warn("機器人關閉被中斷，強制終止");
                Thread.currentThread().interrupt();
            } finally {
                jda = null;
            }
        }
    }
}