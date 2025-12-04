package com.example.catmusic.lyric;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.example.catmusic.bean.Lyric;

import java.util.List;

/**
 * 自定义歌词显示控件
 * 支持歌词高亮显示、滚动效果和现代视觉设计
 */
public class LyricView extends View {
    private static final String TAG = "LyricView";
    
    // 画笔
    private Paint normalPaint; // 普通歌词画笔
    private Paint highlightPaint; // 高亮歌词画笔
    private Paint shadowPaint; // 阴影画笔
    
    // 歌词数据
    private Lyric lyric;
    private int currentLineIndex = -1; // 当前高亮行索引
    private float scrollOffset = 0; // 滚动偏移量
    private long lastUpdateTime = 0; // 上次更新时间
    
    // 动画参数
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();
    private float highlightScale = 1.0f; // 高亮缩放比例
    private float highlightAlpha = 1.0f; // 高亮透明度
    
    // 显示参数
    private int lineHeight = 70; // 行高
    private int textSize = 16; // 字体大小
    private int highlightTextSize = 24; // 高亮字体大小
    private int padding = 30; // 内边距
    private int shadowRadius = 8; // 阴影半径
    
    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化画笔
     */
    private void init() {
        // 普通歌词画笔 - 使用黑色
        normalPaint = new Paint();
        normalPaint.setColor(Color.BLACK);
        normalPaint.setTextSize(spToPx(textSize));
        normalPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        normalPaint.setAntiAlias(true);
        normalPaint.setTextAlign(Paint.Align.CENTER);
        
        // 高亮歌词画笔 - 使用黑色
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.BLACK);
        highlightPaint.setTextSize(spToPx(highlightTextSize));
        highlightPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        highlightPaint.setAntiAlias(true);
        highlightPaint.setTextAlign(Paint.Align.CENTER);
        
        // 阴影画笔 - 为高亮歌词添加立体感
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#80000000"));
        shadowPaint.setTextSize(spToPx(highlightTextSize));
        shadowPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        shadowPaint.setAntiAlias(true);
        shadowPaint.setTextAlign(Paint.Align.CENTER);
        
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 设置歌词数据
     */
    public void setLyric(Lyric lyric) {
        this.lyric = lyric;
        invalidate(); // 重绘视图
    }

    /**
     * 设置当前播放时间，更新高亮行
     */
    public void setCurrentTime(long currentTimeMs) {
        if (lyric == null || !lyric.hasLyric()) {
            currentLineIndex = -1;
            return;
        }
        
        int newIndex = lyric.getCurrentLineIndex(currentTimeMs);
        if (newIndex != currentLineIndex) {
            currentLineIndex = newIndex;
            invalidate(); // 重绘视图
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (lyric == null || !lyric.hasLyric()) {
            // 没有歌词时显示提示
            drawNoLyricText(canvas);
            return;
        }
        
        // 绘制歌词
        drawLyrics(canvas);
    }

    /**
     * 绘制无歌词提示
     */
    private void drawNoLyricText(Canvas canvas) {
        String text = "🎵 暂无歌词 🎵";
        float x = getWidth() / 2f;
        float y = getHeight() / 2f;
        
        // 创建无歌词提示的专用画笔
        Paint noLyricPaint = new Paint();
        noLyricPaint.setColor(Color.parseColor("#666666"));
        noLyricPaint.setTextSize(spToPx(20));
        noLyricPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
        noLyricPaint.setAntiAlias(true);
        noLyricPaint.setTextAlign(Paint.Align.CENTER);
        noLyricPaint.setAlpha(150);
        
        canvas.drawText(text, x, y, noLyricPaint);
    }

    /**
     * 绘制歌词
     */
    private void drawLyrics(Canvas canvas) {
        List<Lyric.LyricLine> lines = lyric.getLyricLines();
        int totalLines = lines.size();
        
        if (totalLines == 0) {
            drawNoLyricText(canvas);
            return;
        }
        
        // 计算显示区域和滚动偏移
        int centerY = getHeight() / 2;
        float targetOffset = currentLineIndex * lineHeight;
        scrollOffset = scrollOffset + (targetOffset - scrollOffset) * 0.1f;
        
        // 绘制所有歌词行，统一显示为黑色
        for (int i = 0; i < totalLines; i++) {
            float y = centerY - scrollOffset + (i - currentLineIndex) * lineHeight;
            
            // 检查是否在可见区域内
            if (y < -lineHeight || y > getHeight() + lineHeight) {
                continue;
            }
            
            String text = lines.get(i).getContent().replace("\n", "").replace("\\n", "");
            float x = getWidth() / 2f;
            
            // 计算歌词行的透明度（根据距离中心位置）
            float distanceFromCenter = Math.abs(y - centerY);
            float alpha = Math.max(0, 1 - distanceFromCenter / (getHeight() / 2));
            
            // 所有歌词统一使用黑色绘制
            normalPaint.setAlpha((int)(alpha * 255));
            canvas.drawText(text, x, y, normalPaint);
        }
    }
    
    /**
     * 更新动画参数
     */
    private void updateAnimation() {
        // 简化动画，只保持基本状态
        highlightScale = 1.0f;
        highlightAlpha = 1.0f;
    }
    
    /**
     * 绘制高亮歌词背景
     */
    private void drawHighlightBackground(Canvas canvas, float x, float y, String text, float alpha) {
        // 计算文本宽度
        float textWidth = highlightPaint.measureText(text);
        float textHeight = highlightPaint.getTextSize();
        
        // 创建渐变背景
        Paint bgPaint = new Paint();
        LinearGradient gradient = new LinearGradient(
                x - textWidth/2 - 20, y - textHeight/2,
                x + textWidth/2 + 20, y + textHeight/2,
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        bgPaint.setAlpha((int)(alpha * 80));
        
        // 绘制圆角矩形背景
        float left = x - textWidth/2 - 20;
        float top = y - textHeight/2 - 8;
        float right = x + textWidth/2 + 20;
        float bottom = y + textHeight/2 + 8;
        float radius = 20;
        
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, bgPaint);
    }

    /**
     * sp转px
     */
    private float spToPx(float sp) {
        float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    /**
     * 设置行高
     */
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        invalidate();
    }

    /**
     * 设置字体大小
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
        normalPaint.setTextSize(spToPx(textSize));
        invalidate();
    }

    /**
     * 设置高亮字体大小
     */
    public void setHighlightTextSize(int highlightTextSize) {
        this.highlightTextSize = highlightTextSize;
        highlightPaint.setTextSize(spToPx(highlightTextSize));
        shadowPaint.setTextSize(spToPx(highlightTextSize));
        invalidate();
    }
    
    /**
     * 设置高亮歌词颜色
     */
    public void setHighlightColor(int color) {
        highlightPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置普通歌词颜色
     */
    public void setNormalColor(int color) {
        normalPaint.setColor(color);
        invalidate();
    }
    
    /**
     * 设置歌词字体
     */
    public void setLyricTypeface(Typeface typeface) {
        normalPaint.setTypeface(typeface);
        highlightPaint.setTypeface(typeface);
        shadowPaint.setTypeface(typeface);
        invalidate();
    }
    
    /**
     * 设置滚动动画速度
     */
    public void setScrollSpeed(float speed) {
        // 速度值应在0.01到0.2之间，值越小滚动越平滑
        // 默认值为0.1
        // 这里可以添加速度控制逻辑
        invalidate();
    }
}