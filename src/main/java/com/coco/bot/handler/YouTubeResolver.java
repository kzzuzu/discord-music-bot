package com.coco.bot.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 媒體 URL 解析器
 *
 * 使用 yt-dlp 解析各平台影片（YouTube、Bilibili 等），獲取直接音頻串流 URL。
 */
@Component
public class YouTubeResolver {
    private static final Logger logger = LoggerFactory.getLogger(YouTubeResolver.class);

    /**
     * 儲存 YouTube 影片資訊的資料類
     * 包含標題、直接音頻 URL 和時長
     */
    public static class TrackInfo {
        /** 影片標題 */
        public final String title;

        /** 直接音頻串流 URL */
        public final String url;

        /** 影片時長（毫秒） */
        public final long duration;

        /**
         * 建構子
         *
         * @param title 影片標題
         * @param url 直接音頻串流 URL
         * @param duration 影片時長（毫秒）
         */
        public TrackInfo(String title, String url, long duration) {
            this.title = title;
            this.url = url;
            this.duration = duration;
        }
    }

    /**
     * 解析任意 yt-dlp 支援的媒體 URL（YouTube、Bilibili 等）
     *
     * @param mediaUrl 媒體網址
     * @return TrackInfo 包含標題、串流 URL 和時長，解析失敗時返回 null
     */
    public TrackInfo resolveUrl(String mediaUrl) {
        return resolveYouTubeUrl(mediaUrl);
    }

    /** @deprecated 請改用 {@link #resolveUrl(String)} */
    public TrackInfo resolveYouTubeUrl(String youtubeUrl) {
        try {
            // 建立 yt-dlp 命令列表
            List<String> command = new ArrayList<>();

            // 使用 python -m yt_dlp 來執行 yt-dlp
            String ytDlpPath = findYtDlpPath();
            String[] pathParts = ytDlpPath.split(",");
            for (String part : pathParts) {
                command.add(part);
            }

            // 添加 yt-dlp 參數
            command.add("--get-title");        // 獲取影片標題
            command.add("--get-url");          // 獲取直接串流 URL
            command.add("--get-duration");     // 獲取影片時長
            command.add("--format");           // 指定格式
            command.add("bestaudio[vcodec=none]/bestaudio[ext=m4a]/bestaudio/best"); // 優先選純音頻串流，避免 DASH 返回多行 URL
            command.add("--no-playlist");      // 只下載單一影片，不處理播放列表
            command.add("--quiet");            // 靜音模式：僅輸出資料到 stdout，進度/警告輸出到 stderr
            command.add("--no-warnings");      // 不輸出警告到 stderr
            command.add(youtubeUrl);

            // 建立並配置 Process
            // 不使用 redirectErrorStream：stdout = 純資料，stderr = 錯誤訊息（分開讀取）
            ProcessBuilder pb = new ProcessBuilder(command);
            // 強制 Python（yt-dlp）以 UTF-8 輸出，避免 Windows 預設 GBK/CP950 亂碼
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");
            Process process = pb.start();

            // 非同步讀取 stderr（避免 stderr buffer 阻塞，同時擷取錯誤訊息）
            List<String> stderrLines = new ArrayList<>();
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader err = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = err.readLine()) != null) {
                        stderrLines.add(line);
                    }
                } catch (Exception ignored) {}
            });
            stderrThread.start();

            // 讀取 stdout（--quiet 模式下僅含 title/url/duration 資料）
            List<String> dataLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        dataLines.add(line);
                    }
                }
            }

            // 等待 process 和 stderr 讀取完成
            int exitCode = process.waitFor();
            stderrThread.join();

            if (exitCode == 0 && dataLines.size() >= 3) {
                // 解析成功，提取資訊
                // yt-dlp 的輸出順序：標題、URL（可能多行，如 DASH）、時長
                // 標題固定在第一行，時長固定在最後一行，取第一條 URL
                String title = dataLines.get(0);
                String directUrl = dataLines.get(1);
                String durationStr = dataLines.get(dataLines.size() - 1);

                // 解析時長字符串為毫秒
                long duration = parseDuration(durationStr);

                return new TrackInfo(title, directUrl, duration);
            } else {
                logger.error("yt-dlp 失敗，退出碼: {}，stdout 行數: {}", exitCode, dataLines.size());
                for (String line : dataLines) {
                    logger.error("yt-dlp stdout: {}", line);
                }
                for (String line : stderrLines) {
                    logger.error("yt-dlp stderr: {}", line);
                }
            }

        } catch (Exception e) {
            // 捕獲任何意外錯誤
            logger.error("YouTube URL 解析錯誤: {}", e.getMessage(), e);
        }

        return null; // 解析失敗
    }

    /**
     * 尋找 yt-dlp 的執行路徑
     *
     * 目前使用 "python -m yt_dlp" 方式，這是最可靠的方法
     * 因為它不依賴於 yt-dlp 的安裝路徑，只要 Python 和 yt-dlp 包存在即可
     *
     * @return yt-dlp 執行命令的字符串（以逗號分隔的格式）
     */
    private String findYtDlpPath() {
        // 使用 python -m yt_dlp 這是最可靠的方法
        // 逗號分隔的格式用於後續 split 操作
        return "python,-m,yt_dlp";
    }

    /**
     * 解析時長字符串為毫秒數
     *
     * yt-dlp 返回的時長格式可能是：
     * - MM:SS（如 "3:45"）
     * - HH:MM:SS（如 "1:23:45"）
     *
     * @param durationStr 時長字符串
     * @return 時長（毫秒），如果解析失敗則返回 0
     */
    private long parseDuration(String durationStr) {
        try {
            // 按冒號分割時間字符串
            String[] parts = durationStr.split(":");
            long totalSeconds = 0;

            if (parts.length == 2) {
                // MM:SS 格式
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                totalSeconds = minutes * 60 + seconds;
            } else if (parts.length == 3) {
                // HH:MM:SS 格式
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                totalSeconds = hours * 3600 + minutes * 60 + seconds;
            }

            // 轉換為毫秒（LavaPlayer 使用毫秒單位）
            return totalSeconds * 1000;

        } catch (NumberFormatException e) {
            // 解析失敗，記錄錯誤並返回 0
            logger.error("無法解析時長: {}", durationStr);
            return 0;
        }
    }
}