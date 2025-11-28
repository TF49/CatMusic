package com.example.catmusic.utils;

import com.example.catmusic.bean.Lyric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词解析工具类
 * 用于解析LRC格式的歌词文件
 */
public class LyricParser {
    
    // LRC歌词时间戳正则表达式，匹配格式如：[mm:ss.xx] 或 [mm:ss]
    private static final Pattern TIME_PATTERN = Pattern.compile("\\[(\\d+):(\\d+)(\\.\\d+)?\\](.*)");
    
    /**
     * 解析歌词字符串
     * 
     * @param lyricString 歌词字符串内容
     * @return 解析后的Lyric对象，如果解析失败返回null
     */
    public static Lyric parseLyric(String lyricString) {
        if (lyricString == null || lyricString.trim().isEmpty()) {
            return null;
        }
        
        List<Lyric.LyricLine> lyricLines = new ArrayList<>();
        
        try {
            // 先清理歌词字符串，移除所有换行符和转义换行符
            String cleanedLyric = lyricString.replaceAll("\\n", " ").replaceAll("\\\\n", " ");
            
            // 处理歌词内容[时间戳]格式
            // 1. 先提取所有时间戳和对应的歌词内容
            // 匹配格式：歌词内容[mm:ss.xx]
            Pattern pattern = Pattern.compile("([\\s\\S]*?)\\[(\\d+):(\\d+)(\\.\\d+)?\\]");
            Matcher matcher = pattern.matcher(cleanedLyric);
            
            while (matcher.find()) {
                try {
                    // 获取歌词内容，去除首尾空格
                    String content = matcher.group(1).trim();
                    
                    // 跳过空内容
                    if (content.isEmpty()) {
                        continue;
                    }
                    
                    // 解析分钟
                    int minutes = Integer.parseInt(matcher.group(2));
                    // 解析秒
                    int seconds = Integer.parseInt(matcher.group(3));
                    // 解析毫秒部分（可选）
                    int milliseconds = 0;
                    if (matcher.group(4) != null) {
                        String millStr = matcher.group(4).substring(1); // 去掉小数点
                        if (millStr.length() == 2) {
                            milliseconds = Integer.parseInt(millStr) * 10; // .20 -> 200毫秒
                        } else if (millStr.length() >= 3) {
                            milliseconds = Integer.parseInt(millStr.substring(0, 3));
                        }
                    }
                    
                    // 计算总毫秒数
                    long totalMilliseconds = minutes * 60 * 1000 + seconds * 1000 + milliseconds;
                    
                    lyricLines.add(new Lyric.LyricLine(totalMilliseconds, content));
                    
                } catch (NumberFormatException e) {
                    // 忽略格式错误的行
                    continue;
                }
            }
            
            // 如果没有解析到有效的歌词行，尝试处理[时间戳]歌词内容格式
            if (lyricLines.isEmpty()) {
                // 匹配格式：[mm:ss.xx]歌词内容
                pattern = Pattern.compile("\\[(\\d+):(\\d+)(\\.\\d+)?\\]([\\s\\S]*?)(?=\\[|$)");
                matcher = pattern.matcher(cleanedLyric);
                
                while (matcher.find()) {
                    try {
                        // 解析分钟
                        int minutes = Integer.parseInt(matcher.group(1));
                        // 解析秒
                        int seconds = Integer.parseInt(matcher.group(2));
                        // 解析毫秒部分（可选）
                        int milliseconds = 0;
                        if (matcher.group(3) != null) {
                            String millStr = matcher.group(3).substring(1); // 去掉小数点
                            if (millStr.length() == 2) {
                                milliseconds = Integer.parseInt(millStr) * 10; // .20 -> 200毫秒
                            } else if (millStr.length() >= 3) {
                                milliseconds = Integer.parseInt(millStr.substring(0, 3));
                            }
                        }
                        
                        // 计算总毫秒数
                        long totalMilliseconds = minutes * 60 * 1000 + seconds * 1000 + milliseconds;
                        
                        // 获取歌词内容，去除首尾空格
                        String content = matcher.group(4).trim();
                        
                        // 跳过空内容或纯时间戳行
                        if (!content.isEmpty()) {
                            lyricLines.add(new Lyric.LyricLine(totalMilliseconds, content));
                        }
                        
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的行
                        continue;
                    }
                }
            }
            
            // 如果仍然没有解析到有效的歌词行，尝试直接使用原始歌词内容
            if (lyricLines.isEmpty()) {
                // 直接将整个歌词作为一行，时间戳为0，去除所有时间戳
                String content = cleanedLyric.replaceAll("\\[.*?\\]", "").trim();
                if (!content.isEmpty()) {
                    lyricLines.add(new Lyric.LyricLine(0, content));
                } else {
                    return null;
                }
            }
            
            return new Lyric(lyricLines);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 从输入流解析歌词
     * 
     * @param inputStream 歌词文件输入流
     * @return 解析后的Lyric对象，如果解析失败返回null
     */
    public static Lyric parseLyric(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        
        StringBuilder lyricBuilder = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lyricBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return parseLyric(lyricBuilder.toString());
    }
    
    /**
     * 检查字符串是否为有效的LRC歌词格式
     * 
     * @param lyricString 歌词字符串
     * @return 如果是有效的LRC格式返回true，否则返回false
     */
    public static boolean isValidLrcFormat(String lyricString) {
        if (lyricString == null || lyricString.trim().isEmpty()) {
            return false;
        }
        
        String[] lines = lyricString.split("\n");
        int validLineCount = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 检查是否包含时间戳格式
            if (TIME_PATTERN.matcher(line).find()) {
                validLineCount++;
            }
        }
        
        // 至少有一行有效的歌词行才认为是有效的LRC格式
        return validLineCount > 0;
    }
    
    /**
     * 获取歌词的元数据信息（如歌曲名、歌手等）
     * 
     * @param lyricString 歌词字符串
     * @param tagName 标签名（如：ti, ar, al等）
     * @return 标签对应的值，如果未找到返回null
     */
    public static String getLyricTag(String lyricString, String tagName) {
        if (lyricString == null || tagName == null) {
            return null;
        }
        
        Pattern tagPattern = Pattern.compile("\\[" + tagName + ":(.+?)\\]");
        Matcher matcher = tagPattern.matcher(lyricString);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
}