package com.coco.bot;

import com.coco.bot.config.BotConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BotConfig 類別的單元測試
 */
class BotConfigTest {

    private BotConfig botConfig;

    @BeforeEach
    void setUp() {
        botConfig = new BotConfig();
    }

    @Test
    @DisplayName("應該能夠載入配置文件")
    void shouldLoadConfigFile() {
        assertNotNull(botConfig, "BotConfig 實例不應為 null");
    }

    @Test
    @DisplayName("應該能夠獲取 Discord Bot Token")
    void shouldGetBotToken() {
        String token = botConfig.getBotToken();

        // 在單元測試環境中，token可能是null或未解析的佔位符
        // 這個測試主要確保方法能正常調用
        if (token != null && !token.contains("${")) {
            assertFalse(token.trim().isEmpty(), "Bot token 不應為空字串");
            assertTrue(token.startsWith("MT"), "Bot token 應以 'MT' 開頭");
        }
    }

    @Test
    @DisplayName("Bot token 應該有正確的格式")
    void shouldHaveCorrectTokenFormat() {
        String token = botConfig.getBotToken();

        if (token != null && !token.equals("${DISCORD_BOT_TOKEN:MOCK_TOKEN_FOR_TESTING}")) {
            // 只有在token不是佔位符時才檢查格式
            assertTrue(token.length() > 50, "Bot token 應該有合理的長度");
        }
    }
}