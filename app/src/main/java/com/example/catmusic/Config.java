package com.example.catmusic;

import android.content.Context;
import android.text.TextUtils;

import com.example.catmusic.utils.LanHostResolver;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 应用配置类
 * 统一管理服务器地址和API接口
 */
public class Config {

    public static final int SERVER_PORT = 3000;
    public static final String API_SERVER_BASE = "api/serverBase";

    private static Context appContext;
    private static volatile String cachedBaseUrl;
    private static final Executor SERVER_BASE_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 在 {@link CatMusicApplication} 中调用，用于读取可选的 {@link R.string#catmusic_server_host} 覆盖。
     */
    public static void init(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
            cachedBaseUrl = null;
        }
    }

    /**
     * 在模拟器等环境下异步请求 {@code /api/serverBase}，将 baseUrl 更新为 Node 所在电脑的局域网 IPv4（与自定义资源 URL 一致）。
     * 用户在 {@link R.string#catmusic_server_host} 中已指定主机时不会发起探测。
     */
    public static void prefetchServerBaseUrlAsync() {
        SERVER_BASE_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (appContext == null) {
                    return;
                }
                if (!TextUtils.isEmpty(appContext.getString(R.string.catmusic_server_host).trim())) {
                    return;
                }
                if (!LanHostResolver.isLikelyEmulatorNatNetwork()) {
                    return;
                }
                String probeUrl = "http://10.0.2.2:" + SERVER_PORT + "/" + API_SERVER_BASE;
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(3, TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder().url(probeUrl).get().build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        return;
                    }
                    String body = response.body().string();
                    JSONObject root = new JSONObject(body);
                    if (root.optInt("code") != 0) {
                        return;
                    }
                    JSONObject result = root.optJSONObject("result");
                    if (result == null) {
                        return;
                    }
                    String baseUrl = result.optString("baseUrl", "").trim();
                    if (TextUtils.isEmpty(baseUrl)) {
                        return;
                    }
                    if (!baseUrl.endsWith("/")) {
                        baseUrl = baseUrl + "/";
                    }
                    updateCachedBaseUrl(baseUrl);
                } catch (Exception ignored) {
                    // 保持 getBaseUrl() 已解析的 10.0.2.2 等回退地址
                }
            }
        });
    }

    static void updateCachedBaseUrl(String baseUrl) {
        synchronized (Config.class) {
            cachedBaseUrl = baseUrl;
        }
    }

    /**
     * 服务器基础 URL，根据当前环境动态解析主机（模拟器为宿主机 10.0.2.2，真机为本机私网 IPv4，可被 strings 覆盖）。
     */
    public static String getBaseUrl() {
        String cached = cachedBaseUrl;
        if (cached != null) {
            return cached;
        }
        synchronized (Config.class) {
            if (cachedBaseUrl != null) {
                return cachedBaseUrl;
            }
            String host = resolveServerHost();
            cachedBaseUrl = "http://" + host + ":" + SERVER_PORT + "/";
            return cachedBaseUrl;
        }
    }

    private static String resolveServerHost() {
        if (appContext != null) {
            String override = appContext.getString(R.string.catmusic_server_host).trim();
            if (!TextUtils.isEmpty(override)) {
                return override;
            }
        }
        return LanHostResolver.resolveServerHost();
    }

    // API接口路径
    public static final String API_GET_RECOMMEND = "api/getRecommend";// 获取推荐歌单
    public static final String API_GET_SONGS_URL = "api/getSongsUrl";// 获取歌曲URL
    public static final String API_GET_ALBUM = "api/getAlbum";// 获取歌单专辑
    public static final String API_GET_LYRIC = "api/getLyric";// 获取歌词

    /**
     * 获取完整的API URL
     */
    public static String getApiUrl(String apiPath) {
        return getBaseUrl() + apiPath;
    }
}
