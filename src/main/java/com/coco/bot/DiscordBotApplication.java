package com.coco.bot;

import com.coco.bot.service.DiscordBotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class DiscordBotApplication implements CommandLineRunner {

    @Autowired
    private DiscordBotService discordBotService;

    private static DiscordBotService staticDiscordBotService;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DiscordBotApplication.class);

        // 設定快速關閉
        app.setRegisterShutdownHook(true);

        // 添加強制關閉鉤子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔴 IDEA 終止檢測到，強制關閉機器人...");
            if (staticDiscordBotService != null) {
                staticDiscordBotService.shutdown();
            }
            // 給一點時間讓關閉完成
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        staticDiscordBotService = discordBotService;
        discordBotService.startBot();
    }
}