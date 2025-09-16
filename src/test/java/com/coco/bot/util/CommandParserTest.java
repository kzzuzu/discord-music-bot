package com.coco.bot.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandParser 單元測試
 */
class CommandParserTest {

    @Test
    @DisplayName("解析正常的播放指令")
    void shouldParseNormalPlayCommand() {
        String message = "!play https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!play", result.getCommand());
        assertEquals(1, result.getArguments().size());
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result.getArguments().get(0));
    }

    @Test
    @DisplayName("解析缺少空格的播放指令")
    void shouldParsePlayCommandWithoutSpace() {
        String message = "!playhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!play", result.getCommand());
        assertEquals(1, result.getArguments().size());
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result.getArguments().get(0));
    }

    @Test
    @DisplayName("解析播放清單指令")
    void shouldParsePlaylistCommand() {
        String message = "!playlist create MyPlaylist https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!playlist", result.getCommand());
        assertEquals(3, result.getArguments().size());
        assertEquals("create", result.getArguments().get(0));
        assertEquals("MyPlaylist", result.getArguments().get(1));
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result.getArguments().get(2));
    }

    @Test
    @DisplayName("解析無效指令應該返回無效結果")
    void shouldReturnInvalidForInvalidCommand() {
        String message = "!invalidcommand test";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertFalse(result.isValid());
        assertNull(result.getCommand());
    }

    @Test
    @DisplayName("解析非指令訊息應該返回無效結果")
    void shouldReturnInvalidForNonCommand() {
        String message = "這不是一個指令";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertFalse(result.isValid());
        assertNull(result.getCommand());
    }

    @Test
    @DisplayName("解析空訊息應該返回無效結果")
    void shouldReturnInvalidForEmptyMessage() {
        String message = "";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertFalse(result.isValid());
        assertNull(result.getCommand());
    }

    @Test
    @DisplayName("解析 null 訊息應該返回無效結果")
    void shouldReturnInvalidForNullMessage() {
        String message = null;
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertFalse(result.isValid());
        assertNull(result.getCommand());
    }

    @Test
    @DisplayName("解析沒有參數的指令")
    void shouldParseCommandWithoutArguments() {
        String message = "!stop";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!stop", result.getCommand());
        assertTrue(result.getArguments().isEmpty());
    }

    @Test
    @DisplayName("解析播放清單子指令")
    void shouldParsePlaylistSubCommand() {
        CommandParser.PlaylistCommand result = CommandParser.parsePlaylistCommand(
            java.util.Arrays.asList("create", "MyPlaylist", "https://example.com")
        );
        
        assertEquals("create", result.getSubCommand());
        assertEquals(2, result.getArguments().size());
        assertEquals("MyPlaylist", result.getArguments().get(0));
        assertEquals("https://example.com", result.getArguments().get(1));
    }

    @Test
    @DisplayName("解析空的播放清單子指令應該返回 help")
    void shouldReturnHelpForEmptyPlaylistCommand() {
        CommandParser.PlaylistCommand result = CommandParser.parsePlaylistCommand(
            java.util.Collections.emptyList()
        );
        
        assertEquals("help", result.getSubCommand());
        assertTrue(result.getArguments().isEmpty());
    }

    @Test
    @DisplayName("格式化時長應該正確")
    void shouldFormatDurationCorrectly() {
        // 測試秒數
        assertEquals("0:30", CommandParser.formatDuration(30000));
        
        // 測試分鐘
        assertEquals("3:45", CommandParser.formatDuration(225000));
        
        // 測試小時
        assertEquals("1:23:45", CommandParser.formatDuration(5025000));
        
        // 測試零或負數
        assertEquals("未知", CommandParser.formatDuration(0));
        assertEquals("未知", CommandParser.formatDuration(-1000));
    }

    @Test
    @DisplayName("解析包含多個 URL 的指令")
    void shouldParseCommandWithMultipleUrls() {
        String message = "!playlist add MyList https://www.youtube.com/watch?v=dQw4w9WgXcQ https://www.youtube.com/watch?v=fJ9rUzIMcZQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!playlist", result.getCommand());
        assertEquals(4, result.getArguments().size());
        assertEquals("add", result.getArguments().get(0));
        assertEquals("MyList", result.getArguments().get(1));
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result.getArguments().get(2));
        assertEquals("https://www.youtube.com/watch?v=fJ9rUzIMcZQ", result.getArguments().get(3));
    }

    @Test
    @DisplayName("解析包含中文字符的指令")
    void shouldParseCommandWithChineseCharacters() {
        String message = "!playlist create 我的最愛 https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!playlist", result.getCommand());
        assertEquals(3, result.getArguments().size());
        assertEquals("create", result.getArguments().get(0));
        assertEquals("我的最愛", result.getArguments().get(1));
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result.getArguments().get(2));
    }

    @Test
    @DisplayName("解析大小寫混合的指令")
    void shouldParseCommandWithMixedCase() {
        String message = "!PLAY https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        CommandParser.ParsedCommand result = CommandParser.parseCommand(message);
        
        assertTrue(result.isValid());
        assertEquals("!play", result.getCommand()); // 應該轉換為小寫
        assertEquals(1, result.getArguments().size());
    }
}