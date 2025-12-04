package com.example.catmusic.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.catmusic.R;
import com.example.catmusic.biz.LyricBiz;

/**
 * 歌词功能测试Activity
 * 用于测试LyricBiz的歌词获取功能
 */
public class LyricTestActivity extends AppCompatActivity {
    private static final String TAG = "LyricTestActivity";
    
    private Button testButton;
    private TextView resultText;
    private LyricBiz lyricBiz;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_test);
        
        initViews();
        lyricBiz = new LyricBiz();
    }
    
    private void initViews() {
        testButton = findViewById(R.id.test_button);
        resultText = findViewById(R.id.result_text);
        
        testButton.setOnClickListener(v -> testLyricFetch());
    }
    
    private void testLyricFetch() {
        resultText.setText("正在获取歌词...");
        
        // 使用一个有效的歌曲mid进行测试
        String testMid = "0039MnYb0qxYhV"; // 示例mid
        
        lyricBiz.getLyric(testMid, new LyricBiz.LyricCallback() {
            @Override
            public void onSuccess(String lyricContent) {
                Log.d(TAG, "歌词获取成功，内容长度: " + (lyricContent != null ? lyricContent.length() : 0));
                runOnUiThread(() -> {
                    if (lyricContent != null && !lyricContent.isEmpty()) {
                        resultText.setText("✅ 歌词获取成功！\n内容长度: " + lyricContent.length() + " 字符\n\n" + 
                                (lyricContent.length() > 100 ? lyricContent.substring(0, 100) + "..." : lyricContent));
                    } else {
                        resultText.setText("⚠️ 歌词内容为空");
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "歌词获取失败: " + e.getMessage());
                runOnUiThread(() -> {
                    resultText.setText("❌ 歌词获取失败: " + e.getMessage());
                });
            }
        });
    }
}