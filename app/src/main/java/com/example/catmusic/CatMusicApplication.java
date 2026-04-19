package com.example.catmusic;

import android.app.Application;

/**
 * 在进程启动时初始化 {@link Config}，以便读取可选的服务器地址覆盖配置。
 */
public class CatMusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Config.init(this);
        Config.prefetchServerBaseUrlAsync();
    }
}
