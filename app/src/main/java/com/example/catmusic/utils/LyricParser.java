package com.example.catmusic.utils;

import com.example.catmusic.bean.Lyric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词解析工具类
 * 用于解析 LRC 格式歌词，同时兼容纯文本歌词的弱同步展示。
 */
public class LyricParser {

    private static final Pattern TIME_TAG_PATTERN = Pattern.compile("\\[(\\d+):(\\d+)(?:\\.(\\d{1,3}))?\\]");
    private static final Pattern LYRIC_TAG_PATTERN = Pattern.compile("\\[(ti|ar|al|by|offset):.*?\\]", Pattern.CASE_INSENSITIVE);

    private LyricParser() {
    }

    /**
     * 解析歌词字符串
     *
     * @param lyricString 歌词字符串内容
     * @return 解析后的 Lyric 对象，如果解析失败返回 null
     */
    public static Lyric parseLyric(String lyricString) {
        if (lyricString == null || lyricString.trim().isEmpty()) {
            return null;
        }

        String normalized = lyricString.replace("\r\n", "\n").replace('\r', '\n');
        String[] rawLines = normalized.split("\n");

        List<Lyric.LyricLine> preciseLines = parseTimedLines(rawLines);
        if (!preciseLines.isEmpty()) {
            return new Lyric(preciseLines, Lyric.SyncType.PRECISE_LRC);
        }

        List<Lyric.LyricLine> plainLines = parsePlainTextLines(rawLines);
        if (plainLines.isEmpty()) {
            return null;
        }
        return new Lyric(plainLines, Lyric.SyncType.PLAIN_TEXT);
    }

    private static List<Lyric.LyricLine> parseTimedLines(String[] rawLines) {
        List<Lyric.LyricLine> lyricLines = new ArrayList<>();
        for (String rawLine : rawLines) {
            if (rawLine == null) {
                continue;
            }
            String line = rawLine.trim();
            if (line.isEmpty() || LYRIC_TAG_PATTERN.matcher(line).matches()) {
                continue;
            }

            Matcher matcher = TIME_TAG_PATTERN.matcher(line);
            List<Long> timestamps = new ArrayList<>();
            int lastTagEnd = -1;
            while (matcher.find()) {
                timestamps.add(parseTimeToMillis(matcher.group(1), matcher.group(2), matcher.group(3)));
                lastTagEnd = matcher.end();
            }

            if (timestamps.isEmpty()) {
                continue;
            }

            String content = "";
            if (lastTagEnd >= 0 && lastTagEnd <= line.length()) {
                content = line.substring(lastTagEnd).trim();
            }

            // 兼容少量非标准格式：歌词内容在前，时间戳在后
            if (content.isEmpty()) {
                Matcher reverseMatcher = TIME_TAG_PATTERN.matcher(line);
                int firstTagStart = line.length();
                while (reverseMatcher.find()) {
                    firstTagStart = Math.min(firstTagStart, reverseMatcher.start());
                }
                if (firstTagStart > 0 && firstTagStart <= line.length()) {
                    content = line.substring(0, firstTagStart).trim();
                }
            }

            if (content.isEmpty()) {
                continue;
            }

            for (Long timestamp : timestamps) {
                lyricLines.add(new Lyric.LyricLine(timestamp, content));
            }
        }
        return lyricLines;
    }

    private static List<Lyric.LyricLine> parsePlainTextLines(String[] rawLines) {
        List<Lyric.LyricLine> lyricLines = new ArrayList<>();
        long time = 0L;
        long step = 3000L;
        for (String rawLine : rawLines) {
            if (rawLine == null) {
                continue;
            }
            String content = rawLine.trim();
            if (content.isEmpty() || LYRIC_TAG_PATTERN.matcher(content).matches()) {
                continue;
            }
            lyricLines.add(new Lyric.LyricLine(time, content));
            time += step;
        }
        return lyricLines;
    }

    private static long parseTimeToMillis(String minuteText, String secondText, String fractionText) {
        int minutes = parseSafeInt(minuteText);
        int seconds = parseSafeInt(secondText);
        int milliseconds = 0;
        if (fractionText != null && !fractionText.isEmpty()) {
            String normalizedFraction = fractionText;
            if (fractionText.length() == 1) {
                normalizedFraction = fractionText + "00";
            } else if (fractionText.length() == 2) {
                normalizedFraction = fractionText + "0";
            } else if (fractionText.length() > 3) {
                normalizedFraction = fractionText.substring(0, 3);
            }
            milliseconds = parseSafeInt(normalizedFraction);
        }
        return minutes * 60_000L + seconds * 1000L + milliseconds;
    }

    private static int parseSafeInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 从输入流解析歌词
     *
     * @param inputStream 歌词文件输入流
     * @return 解析后的 Lyric 对象，如果解析失败返回 null
     */
    public static Lyric parseLyric(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        StringBuilder lyricBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lyricBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            return null;
        }
        return parseLyric(lyricBuilder.toString());
    }

    /**
     * 检查字符串是否为有效的 LRC 歌词格式
     *
     * @param lyricString 歌词字符串
     * @return 如果是有效的 LRC 格式返回 true，否则返回 false
     */
    public static boolean isValidLrcFormat(String lyricString) {
        if (lyricString == null || lyricString.trim().isEmpty()) {
            return false;
        }
        String[] lines = lyricString.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String line : lines) {
            if (line != null && TIME_TAG_PATTERN.matcher(line.trim()).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取歌词的元数据信息（如歌曲名、歌手等）
     *
     * @param lyricString 歌词字符串
     * @param tagName 标签名（如：ti, ar, al 等）
     * @return 标签对应的值，如果未找到返回 null
     */
    public static String getLyricTag(String lyricString, String tagName) {
        if (lyricString == null || tagName == null) {
            return null;
        }

        Pattern tagPattern = Pattern.compile("\\[" + tagName + ":(.+?)\\]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = tagPattern.matcher(lyricString);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
