package com.coco.bot.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * YouTube URL 解析器
 *
 * 這個類使用 yt-dlp 工具來解析 YouTube 影片，獲取直接的音頻串流 URL
 * 這樣可以繞過 LavaPlayer 在處理 YouTube 時遇到的反機器人保護問題
 *
 * 主要功能：
 * - 解析 YouTube URL 獲取影片標題
 * - 獲取直接音頻串流連結（最高品質音頻）
 * - 解析影片時長
 *
 * 技術實現：
 * - 使用 ProcessBuilder 執行 yt-dlp 命令
 * - 解析 yt-dlp 的輸出結果
 * - 處理各種錯誤情況
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
     * 解析 YouTube URL 獲取影片資訊
     *
     * 這個方法使用 yt-dlp 工具來解析 YouTube 影片，獲取：
     * 1. 影片標題
     * 2. 直接音頻串流 URL（最高品質）
     * 3. 影片時長
     *
     * @param youtubeUrl YouTube 影片網址
     * @return TrackInfo 物件包含影片資訊，如果解析失敗則返回 null
     */
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
            command.add("bestaudio/best");     // 最佳音頻品質，如果沒有則使用最佳品質
            command.add("--no-playlist");      // 只下載單一影片，不處理播放列表
            command.add(youtubeUrl);           // YouTube URL

            // 建立並配置 Process
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 將錯誤輸出重定向到標準輸出
            Process process = pb.start();

            // 讀取 yt-dlp 的輸出
            List<String> output = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                }
            }

            // 等待 process 完成並檢查退出代碼
            int exitCode = process.waitFor();

            if (exitCode == 0 && output.size() >= 3) {
                // 解析成功，提取資訊
                // yt-dlp 的輸出順序：標題、URL、時長
                String title = output.get(0);
                String directUrl = output.get(1);
                String durationStr = output.get(2);

                // 解析時長字符串為毫秒
                long duration = parseDuration(durationStr);

                return new TrackInfo(title, directUrl, duration);
            } else {
                // 解析失敗，記錄錯誤資訊
                logger.error("yt-dlp 失敗，退出碼: {}", exitCode);
                for (String line : output) {
                    logger.error("yt-dlp 輸出: {}", line);
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