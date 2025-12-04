package com.example.catmusic.utils;

/**
 * 时间格式化工具类
 * 用于处理歌词时间格式的转换
 */
public class TimeUtils {
    
    /**
     * 将LRC格式的时间字符串转换为毫秒
     * 支持格式：[mm:ss.xx] 或 [mm:ss]
     * 
     * @param timeString LRC时间格式字符串，如 [03:45.20] 或 [03:45]
     * @return 毫秒时间
     */
    public static long formatTimeToMill(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        
        try {
            // 移除方括号
            String time = timeString.replace("[", "").replace("]", "");
            
            // 分割分钟和秒
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return 0;
            }
            
            int minutes = Integer.parseInt(parts[0]);
            
            // 处理秒和毫秒
            String secondsPart = parts[1];
            int seconds;
            int milliseconds = 0;
            
            if (secondsPart.contains(".")) {
                String[] secParts = secondsPart.split("\\.");
                seconds = Integer.parseInt(secParts[0]);
                // 毫秒部分，如 .20 转为 200毫秒
                String millStr = secParts[1];
                if (millStr.length() == 2) {
                    milliseconds = Integer.parseInt(millStr) * 10;
                } else if (millStr.length() >= 3) {
                    milliseconds = Integer.parseInt(millStr.substring(0, 3));
                }
            } else {
                seconds = Integer.parseInt(secondsPart);
            }
            
            return minutes * 60 * 1000 + seconds * 1000 + milliseconds;
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 将毫秒时间格式化为LRC格式的时间字符串
     * 
     * @param timeMs 毫秒时间
     * @return LRC格式的时间字符串，如 [03:45.20]
     */
    public static String formatMillToTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long milliseconds = timeMs % 1000;
        
        return String.format("[%02d:%02d.%02d]", minutes, seconds, milliseconds / 10);
    }
    
    /**
     * 将毫秒时间格式化为显示格式（mm:ss）
     * 
     * @param timeMs 毫秒时间
     * @return 显示格式的时间字符串，如 03:45
     */
    public static String formatTimeDisplay(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * 将毫秒时间格式化为详细显示格式（mm:ss.ms）
     * 
     * @param timeMs 毫秒时间
     * @return 详细显示格式的时间字符串，如 03:45.20
     */
    public static String formatTimeDisplayDetailed(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long milliseconds = timeMs % 1000;
        
        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds / 10);
    }
}