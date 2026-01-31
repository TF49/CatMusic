package com.example.catmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.catmusic.bean.SongsList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 音乐收藏管理
 * 使用 SharedPreferences + Gson 持久化收藏的歌曲列表，按 mid 去重。
 */
public class FavoriteManager {
    private static final String PREF_NAME = "catmusic_favorites";
    private static final String KEY_FAVORITE_JSON = "favorite_songs_json";

    private final SharedPreferences prefs;
    private final Gson gson;
    private List<SongsList.ResultBean.SongsBean> cache;

    public FavoriteManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        cache = loadFromPrefs();
    }

    private List<SongsList.ResultBean.SongsBean> loadFromPrefs() {
        String json = prefs.getString(KEY_FAVORITE_JSON, "[]");
        Type type = new TypeToken<ArrayList<SongsList.ResultBean.SongsBean>>() {}.getType();
        try {
            List<SongsList.ResultBean.SongsBean> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            LogUtil.e("FavoriteManager", "load favorites error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveToPrefs(List<SongsList.ResultBean.SongsBean> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_FAVORITE_JSON, json).apply();
        cache = new ArrayList<>(list);
    }

    /** 是否已收藏（按 mid 判断） */
    public boolean isFavorite(SongsList.ResultBean.SongsBean song) {
        if (song == null || song.getMid() == null) return false;
        String mid = song.getMid();
        for (SongsList.ResultBean.SongsBean s : cache) {
            if (mid.equals(s.getMid())) return true;
        }
        return false;
    }

    /** 添加收藏，若已存在则忽略 */
    public void addFavorite(SongsList.ResultBean.SongsBean song) {
        if (song == null) return;
        if (isFavorite(song)) return;
        List<SongsList.ResultBean.SongsBean> list = new ArrayList<>(cache);
        list.add(song);
        saveToPrefs(list);
    }

    /** 取消收藏（按 mid） */
    public void removeFavorite(SongsList.ResultBean.SongsBean song) {
        if (song == null || song.getMid() == null) return;
        String mid = song.getMid();
        List<SongsList.ResultBean.SongsBean> list = new ArrayList<>();
        for (SongsList.ResultBean.SongsBean s : cache) {
            if (!mid.equals(s.getMid())) list.add(s);
        }
        saveToPrefs(list);
    }

    /** 切换收藏状态，返回当前是否已收藏 */
    public boolean toggleFavorite(SongsList.ResultBean.SongsBean song) {
        if (song == null) return false;
        if (isFavorite(song)) {
            removeFavorite(song);
            return false;
        } else {
            addFavorite(song);
            return true;
        }
    }

    /** 获取所有收藏（返回副本） */
    public List<SongsList.ResultBean.SongsBean> getAllFavorites() {
        return new ArrayList<>(cache);
    }

    /** 收藏数量 */
    public int getFavoriteCount() {
        return cache.size();
    }
}
