package com.coco.bot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指令解析工具類
 * 處理 Discord 機器人指令的解析，包括自動處理缺少空格的情況
 */
public class CommandParser {
    private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);
    
    // 支援的指令列表
    private static final List<String> COMMANDS = Arrays.asList(
        "!play", "!stop", "!pause", "!resume", "!skip", "!queue", "!help", "!playlist"
    );
    
    // URL 模式匹配
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://(www\\.)?(youtube\\.com|youtu\\.be|soundcloud\\.com|spotify\\.com)[^\\s]+)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析指令和參數
     * 
     * @param message 原始訊息
     * @return ParsedCommand 物件
     */
    public static ParsedCommand parseCommand(String message) {
        if (message == null || message.trim().isEmpty() || !message.startsWith("!")) {
            return new ParsedCommand(null, new ArrayList<>(), message);
        }
        
        message = message.trim();
        logger.debug("解析指令: {}", message);
        
        // 首先嘗試正常的空格分割
        String[] parts = message.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        
        // 檢查是否為有效指令
        if (COMMANDS.contains(command)) {
            List<String> args = new ArrayList<>();
            if (parts.length > 1) {
                args.addAll(parseArguments(parts[1]));
            }
            return new ParsedCommand(command, args, message);
        }
        
        // 如果不是有效指令，可能是缺少空格的情況
        ParsedCommand fixedCommand = fixMissingSpaces(message);
        if (fixedCommand != null) {
            logger.info("自動修正缺少空格的指令: {} -> {} {}", 
                message, fixedCommand.getCommand(), String.join(" ", fixedCommand.getArguments()));
            return fixedCommand;
        }
        
        // 如果都不是，返回無效指令
        return new ParsedCommand(null, new ArrayList<>(), message);
    }

    /**
     * 修正缺少空格的指令
     * 
     * @param message 原始訊息
     * @return 修正後的 ParsedCommand，如果無法修正則返回 null
     */
    private static ParsedCommand fixMissingSpaces(String message) {
        for (String cmd : COMMANDS) {
            if (message.toLowerCase().startsWith(cmd) && message.length() > cmd.length()) {
                String remaining = message.substring(cmd.length());
                
                // 檢查剩餘部分是否看起來像參數
                if (isValidArgument(remaining)) {
                    List<String> args = parseArguments(remaining);
                    return new ParsedCommand(cmd, args, message);
                }
            }
        }
        return null;
    }

    /**
     * 檢查字串是否看起來像有效的參數
     * 
     * @param arg 參數字串
     * @return true 如果看起來像有效參數
     */
    private static boolean isValidArgument(String arg) {
        if (arg == null || arg.trim().isEmpty()) {
            return false;
        }
        
        // 檢查是否為 URL
        if (URL_PATTERN.matcher(arg).find()) {
            return true;
        }
        
        // 檢查是否為播放清單相關參數
        if (arg.matches(".*[a-zA-Z0-9].*")) {
            return true;
        }
        
        return false;
    }

    /**
     * 解析參數字串
     * 
     * @param argString 參數字串
     * @return 參數列表
     */
    private static List<String> parseArguments(String argString) {
        List<String> args = new ArrayList<>();
        
        if (argString == null || argString.trim().isEmpty()) {
            return args;
        }
        
        argString = argString.trim();
        
        // 檢查是否包含 URL
        Matcher urlMatcher = URL_PATTERN.matcher(argString);
        if (urlMatcher.find()) {
            // 如果有 URL，先處理 URL 前的部分
            String beforeUrl = argString.substring(0, urlMatcher.start()).trim();
            if (!beforeUrl.isEmpty()) {
                args.addAll(Arrays.asList(beforeUrl.split("\\s+")));
            }
            
            // 添加 URL
            args.add(urlMatcher.group(1));
            
            // 處理 URL 後的部分
            String afterUrl = argString.substring(urlMatcher.end()).trim();
            if (!afterUrl.isEmpty()) {
                args.addAll(Arrays.asList(afterUrl.split("\\s+")));
            }
        } else {
            // 沒有 URL，正常分割
            args.addAll(Arrays.asList(argString.split("\\s+")));
        }
        
        return args;
    }

    /**
     * 解析播放清單指令
     * 
     * @param args 參數列表
     * @return PlaylistCommand 物件
     */
    public static PlaylistCommand parsePlaylistCommand(List<String> args) {
        if (args.isEmpty()) {
            return new PlaylistCommand("help", new ArrayList<>());
        }
        
        String subCommand = args.get(0).toLowerCase();
        List<String> subArgs = args.subList(1, args.size());
        
        return new PlaylistCommand(subCommand, subArgs);
    }

    /**
     * 格式化時長顯示
     * 
     * @param duration 時長（毫秒）
     * @return 格式化的時長字串
     */
    public static String formatDuration(long duration) {
        if (duration <= 0) {
            return "未知";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * 解析後的指令物件
     */
    public static class ParsedCommand {
        private final String command;
        private final List<String> arguments;
        private final String originalMessage;

        public ParsedCommand(String command, List<String> arguments, String originalMessage) {
            this.command = command;
            this.arguments = arguments != null ? arguments : new ArrayList<>();
            this.originalMessage = originalMessage;
        }

        public String getCommand() {
            return command;
        }

        public List<String> getArguments() {
            return new ArrayList<>(arguments);
        }

        public String getOriginalMessage() {
            return originalMessage;
        }

        public boolean isValid() {
            return command != null && COMMANDS.contains(command);
        }

        public String getArgumentsAsString() {
            return String.join(" ", arguments);
        }

        @Override
        public String toString() {
            return "ParsedCommand{" +
                    "command='" + command + '\'' +
                    ", arguments=" + arguments +
                    ", originalMessage='" + originalMessage + '\'' +
                    '}';
        }
    }

    /**
     * 播放清單指令物件
     */
    public static class PlaylistCommand {
        private final String subCommand;
        private final List<String> arguments;

        public PlaylistCommand(String subCommand, List<String> arguments) {
            this.subCommand = subCommand;
            this.arguments = arguments != null ? arguments : new ArrayList<>();
        }

        public String getSubCommand() {
            return subCommand;
        }

        public List<String> getArguments() {
            return new ArrayList<>(arguments);
        }

        public String getArgumentsAsString() {
            return String.join(" ", arguments);
        }

        @Override
        public String toString() {
            return "PlaylistCommand{" +
                    "subCommand='" + subCommand + '\'' +
                    ", arguments=" + arguments +
                    '}';
        }
    }
}