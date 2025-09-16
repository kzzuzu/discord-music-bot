package com.coco.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * 所有測試的測試套件
 */
@Suite
@SelectClasses({
    BotConfigTest.class,
    MusicQueueTest.class,
    AudioPlayerSendHandlerTest.class,
    YouTubeResolverTest.class
})
@DisplayName("Discord 音樂機器人測試套件")
public class AllTests {
    // 這個類別用於組織和執行所有測試
    // 不需要包含任何代碼，註解已經處理了所有事情
}