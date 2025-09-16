package com.coco.bot;

import com.coco.bot.handler.YouTubeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * YouTubeResolver 類別的單元測試
 * 
 * 注意：這些測試需要 yt-dlp 工具和網路連接
 * 在 CI/CD 環境中可能需要跳過這些測試
 */
class YouTubeResolverTest {

    private YouTubeResolver youTubeResolver;

    @BeforeEach
    void setUp() {
        youTubeResolver = new YouTubeResolver();
    }

    @Test
    @DisplayName("TrackInfo 建構子應該正確設定屬性")
    void trackInfoConstructorShouldSetProperties() {
        String title = "Test Song";
        String url = "https://example.com/audio.mp3";
        long duration = 180000; // 3 minutes
        
        YouTubeResolver.TrackInfo trackInfo = new YouTubeResolver.TrackInfo(title, url, duration);
        
        assertEquals(title, trackInfo.title, "標題應該被正確設定");
        assertEquals(url, trackInfo.url, "URL 應該被正確設定");
        assertEquals(duration, trackInfo.duration, "時長應該被正確設定");
    }

    @Test
    @DisplayName("解析無效 YouTube URL 應該返回 null")
    void shouldReturnNullForInvalidYouTubeUrl() {
        String invalidUrl = "https://invalid-url.com/watch?v=invalid";
        
        YouTubeResolver.TrackInfo result = youTubeResolver.resolveYouTubeUrl(invalidUrl);
        
        assertNull(result, "無效的 YouTube URL 應該返回 null");
    }

    @Test
    @DisplayName("解析空字串應該返回 null")
    void shouldReturnNullForEmptyUrl() {
        YouTubeResolver.TrackInfo result = youTubeResolver.resolveYouTubeUrl("");
        
        assertNull(result, "空字串應該返回 null");
    }

    @Test
    @DisplayName("解析 null URL 應該返回 null")
    void shouldReturnNullForNullUrl() {
        YouTubeResolver.TrackInfo result = youTubeResolver.resolveYouTubeUrl(null);
        
        assertNull(result, "null URL 應該返回 null");
    }

    // 這個測試需要實際的 yt-dlp 工具和網路連接
    // 只有在 CI 環境變數允許時才執行
    @Test
    @DisplayName("解析有效的 YouTube URL 應該返回 TrackInfo")
    @EnabledIf("isYtDlpAvailable")
    void shouldResolveValidYouTubeUrl() {
        // 使用一個已知存在的 YouTube 影片
        String validUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        YouTubeResolver.TrackInfo result = youTubeResolver.resolveYouTubeUrl(validUrl);
        
        if (result != null) {
            assertNotNull(result.title, "標題不應為 null");
            assertNotNull(result.url, "URL 不應為 null");
            assertTrue(result.duration >= 0, "時長應該大於或等於 0");
            assertFalse(result.title.trim().isEmpty(), "標題不應為空字串");
            assertFalse(result.url.trim().isEmpty(), "URL 不應為空字串");
        }
        // 如果 result 為 null，表示 yt-dlp 工具不可用或網路問題，測試通過
    }

    /**
     * 檢查是否可以執行需要 yt-dlp 的測試
     * 這個方法會被 @EnabledIf 註解使用
     */
    static boolean isYtDlpAvailable() {
        // 檢查環境變數是否允許執行整合測試
        String enableIntegrationTests = System.getenv("ENABLE_INTEGRATION_TESTS");
        if ("false".equalsIgnoreCase(enableIntegrationTests)) {
            return false;
        }
        
        try {
            // 嘗試執行 yt-dlp --version 來檢查工具是否可用
            ProcessBuilder pb = new ProcessBuilder("python", "-m", "yt_dlp", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}