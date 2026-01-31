package com.example.catmusic.utils;

import android.util.Log;

/**
 * 统一日志工具，用于控制 Logcat 输出量、优化性能。
 * - 仅当 VERBOSE=true 时输出 Log.d，默认关闭以减少 Logcat 刷屏。
 * - Log.w / Log.e 始终输出，便于排查问题。
 */
public final class LogUtil {

    /** 设为 true 时才会输出 Log.d，日常开发建议保持 false 以减少 Logcat 刷屏 */
    public static boolean VERBOSE = false;

    public static void d(String tag, String msg) {
        if (VERBOSE && msg != null) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (msg != null) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (msg != null) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (msg != null) {
            Log.e(tag, msg, tr);
        }
    }

    private LogUtil() {}
}
