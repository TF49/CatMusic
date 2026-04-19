package com.example.catmusic.biz;

import com.example.catmusic.utils.LogUtil;

import com.example.catmusic.Config;

import org.json.JSONException;
import org.json.JSONObject;

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
        String url = Config.getApiUrl(Config.API_GET_LYRIC) + "?mid=" + mid;
        LogUtil.d(TAG, "请求歌词URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "获取歌词失败: " + e.getMessage());
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorMsg = "获取歌词失败，响应码: " + response.code();
                    LogUtil.e(TAG, errorMsg);
                    callback.onFailure(new IOException(errorMsg));
                    return;
                }

                String jsonString = response.body().string();
                LogUtil.d(TAG, "获取歌词成功，原始内容长度: " + jsonString.length());

                try {
                    JSONObject root = new JSONObject(jsonString);
                    int code = root.optInt("code", -1);
                    if (code != 0) {
                        String msg = root.optString("message", "歌词接口返回错误，code=" + code);
                        callback.onFailure(new IOException(msg));
                        return;
                    }

                    JSONObject result = root.optJSONObject("result");
                    if (result == null) {
                        callback.onFailure(new IOException("歌词数据字段缺失"));
                        return;
                    }

                    String lyricContent = result.optString("lyric", "");
                    if (lyricContent == null || lyricContent.isEmpty()) {
                        callback.onFailure(new IOException("歌词内容为空"));
                        return;
                    }

                    LogUtil.d(TAG, "解析歌词成功，歌词长度: " + lyricContent.length());
                    callback.onSuccess(lyricContent);
                } catch (JSONException e) {
                    LogUtil.e(TAG, "解析歌词JSON失败: " + e.getMessage());
                    callback.onFailure(e);
                }
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