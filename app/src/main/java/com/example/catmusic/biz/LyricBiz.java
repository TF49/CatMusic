package com.example.catmusic.biz;

import android.util.Log;

import com.example.catmusic.Config;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 歌词业务类
 * 负责获取歌词数据
 */
public class LyricBiz {
    private static final String TAG = "LyricBiz";
    private OkHttpClient okHttpClient;

    public LyricBiz() {
        this.okHttpClient = new OkHttpClient();
    }

    public LyricBiz(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 获取歌词
     * @param mid 歌曲mid
     * @param callback 回调接口
     */
    public void getLyric(String mid, LyricCallback callback) {
        if (mid == null || mid.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("歌曲mid不能为空"));
            return;
        }

        // 使用正确的配置常量和URL构建方式
        String url = Config.BASE_URL + "api/getLyric?mid=" + mid;
        Log.d(TAG, "请求歌词URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "获取歌词失败: " + e.getMessage());
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorMsg = "获取歌词失败，响应码: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onFailure(new IOException(errorMsg));
                    return;
                }

                String lyricContent = response.body().string();
                Log.d(TAG, "获取歌词成功，内容长度: " + lyricContent.length());
                callback.onSuccess(lyricContent);
            }
        });
    }

    /**
     * 歌词回调接口
     */
    public interface LyricCallback {
        void onSuccess(String lyricContent);
        void onFailure(Exception e);
    }
}