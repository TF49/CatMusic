package com.example.catmusic.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 歌词数据模型类
 * 用于存储和管理歌词数据
 */
public class Lyric {
    
    /**
     * 歌词行数据模型
     */
    public static class LyricLine {
        private long time; // 时间戳（毫秒）
        private String content; // 歌词内容
        
        public LyricLine(long time, String content) {
            this.time = time;
            this.content = content;
        }
        
        public long getTime() {
            return time;
        }
        
        public void setTime(long time) {
            this.time = time;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "LyricLine{" +
                    "time=" + time +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
    
    private List<LyricLine> lyricLines;
    
    public Lyric() {
        this.lyricLines = new ArrayList<>();
    }
    
    public Lyric(List<LyricLine> lyricLines) {
        this.lyricLines = lyricLines;
        sortLyricLines();
    }
    
    /**
     * 添加歌词行
     */
    public void addLyricLine(LyricLine line) {
        lyricLines.add(line);
        sortLyricLines();
    }
    
    /**
     * 添加歌词行
     */
    public void addLyricLine(long time, String content) {
        lyricLines.add(new LyricLine(time, content));
        sortLyricLines();
    }
    
    /**
     * 获取歌词行列表
     */
    public List<LyricLine> getLyricLines() {
        return lyricLines;
    }
    
    /**
     * 设置歌词行列表
     */
    public void setLyricLines(List<LyricLine> lyricLines) {
        this.lyricLines = lyricLines;
        sortLyricLines();
    }
    
    /**
     * 根据时间戳获取当前歌词行索引
     */
    public int getCurrentLineIndex(long currentTime) {
        if (lyricLines.isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < lyricLines.size(); i++) {
            if (currentTime < lyricLines.get(i).getTime()) {
                return i - 1;
            }
        }
        
        return lyricLines.size() - 1;
    }
    
    /**
     * 根据时间戳获取当前歌词行
     */
    public LyricLine getCurrentLine(long currentTime) {
        int index = getCurrentLineIndex(currentTime);
        if (index >= 0 && index < lyricLines.size()) {
            return lyricLines.get(index);
        }
        return null;
    }
    
    /**
     * 获取歌词行数量
     */
    public int getLineCount() {
        return lyricLines.size();
    }
    
    /**
     * 检查是否为空歌词
     */
    public boolean isEmpty() {
        return lyricLines.isEmpty();
    }
    
    /**
     * 检查是否有歌词（hasLyric是isEmpty的反向方法）
     */
    public boolean hasLyric() {
        return !lyricLines.isEmpty();
    }
    
    /**
     * 对歌词行按时间戳排序
     */
    private void sortLyricLines() {
        Collections.sort(lyricLines, new Comparator<LyricLine>() {
            @Override
            public int compare(LyricLine line1, LyricLine line2) {
                return Long.compare(line1.getTime(), line2.getTime());
            }
        });
    }
    
    /**
     * 获取歌词总时长（毫秒）
     */
    public long getTotalDuration() {
        if (lyricLines.isEmpty()) {
            return 0;
        }
        return lyricLines.get(lyricLines.size() - 1).getTime();
    }
    
    @Override
    public String toString() {
        return "Lyric{" +
                "lyricLines=" + lyricLines +
                '}';
    }
}