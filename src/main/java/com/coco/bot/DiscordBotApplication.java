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

    public static void main(String[] args) {
        SpringApplication.run(DiscordBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        discordBotService.startBot();
    }
}
