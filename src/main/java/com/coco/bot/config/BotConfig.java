package com.coco.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 機器人配置管理類
 * 使用 Spring Boot 配置管理
 */
@Configuration
public class BotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    /**
     * 獲取 Discord Bot Token
     *
     * @return Discord Bot Token
     */
    public String getBotToken() {
        return botToken;
    }
}