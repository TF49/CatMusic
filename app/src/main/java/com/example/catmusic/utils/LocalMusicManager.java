package com.example.catmusic.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.catmusic.bean.SongsList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/**
 * 本地歌曲库管理器。
 * 使用 SharedPreferences + Gson 持久化用户导入的本地歌曲元数据。
 */
public class LocalMusicManager {
    public static final long LOCAL_LIBRARY_ALBUM_ID = -20260418L;
    private static final String PREF_NAME = "catmusic_local_music";
    private static final String KEY_LOCAL_MUSIC_JSON = "local_music_json";
    public static final String LOCAL_ALBUM_NAME = "本地导入";

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<SongsList.ResultBean.SongsBean> cache;

    public LocalMusicManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.cache = loadFromPrefs();
    }

    public List<SongsList.ResultBean.SongsBean> getAllImportedSongs() {
        return new ArrayList<>(cache);
    }

    public int upsertImportedSongs(List<SongsList.ResultBean.SongsBean> songs) {
        if (songs == null || songs.isEmpty()) {
            return 0;
        }
        List<SongsList.ResultBean.SongsBean> merged = new ArrayList<>(cache);
        int updated = 0;
        for (SongsList.ResultBean.SongsBean song : songs) {
            if (song == null || song.getMid() == null || song.getMid().isEmpty()) {
                continue;
            }
            int existingIndex = indexOfMid(merged, song.getMid());
            if (existingIndex >= 0) {
                merged.set(existingIndex, song);
            } else {
                merged.add(song);
            }
            updated++;
        }
        saveToPrefs(merged);
        return updated;
    }

    public SongsList.ResultBean.SongsBean buildSongFromAudioUri(Uri audioUri, String lyricUriText) {
        if (audioUri == null) {
            return null;
        }

        SongsList.ResultBean.SongsBean song = new SongsList.ResultBean.SongsBean();
        song.setLocal(true);
        song.setMid(buildStableMid(audioUri));
        song.setLocalAudioUri(audioUri.toString());
        song.setLocalLyricUri(lyricUriText);
        song.setAlbum(LOCAL_ALBUM_NAME);
        song.setUrl("");
        song.setPic("");
        song.setLyricPrecise(lyricUriText != null && lyricUriText.toLowerCase(Locale.ROOT).endsWith(".lrc"));

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, audioUri);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            song.setName(isBlank(title) ? getDisplayName(audioUri) : title);
            song.setSinger(isBlank(artist) ? "本地导入" : artist);
            song.setDuration(parseDurationSeconds(duration));
        } catch (Exception e) {
            LogUtil.w("LocalMusicManager", "读取本地音频元数据失败: " + e.getMessage());
            song.setName(getDisplayName(audioUri));
            song.setSinger("本地导入");
            song.setDuration(0);
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
        return song;
    }

    private List<SongsList.ResultBean.SongsBean> loadFromPrefs() {
        String json = prefs.getString(KEY_LOCAL_MUSIC_JSON, "[]");
        Type type = new TypeToken<ArrayList<SongsList.ResultBean.SongsBean>>() {}.getType();
        try {
            List<SongsList.ResultBean.SongsBean> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            LogUtil.e("LocalMusicManager", "load imported songs error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveToPrefs(List<SongsList.ResultBean.SongsBean> list) {
        prefs.edit().putString(KEY_LOCAL_MUSIC_JSON, gson.toJson(list)).apply();
        cache = new ArrayList<>(list);
    }

    private int indexOfMid(List<SongsList.ResultBean.SongsBean> songs, String mid) {
        for (int i = 0; i < songs.size(); i++) {
            SongsList.ResultBean.SongsBean song = songs.get(i);
            if (song != null && mid.equals(song.getMid())) {
                return i;
            }
        }
        return -1;
    }

    private String buildStableMid(Uri audioUri) {
        return "local_" + UUID.nameUUIDFromBytes(audioUri.toString().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }

    private String getDisplayName(Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try (android.database.Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String displayName = cursor.getString(nameIndex);
                    if (!isBlank(displayName)) {
                        int dotIndex = displayName.lastIndexOf('.');
                        return dotIndex > 0 ? displayName.substring(0, dotIndex) : displayName;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.w("LocalMusicManager", "读取文件名失败: " + e.getMessage());
        }
        return "本地歌曲";
    }

    private int parseDurationSeconds(String durationText) {
        if (isBlank(durationText)) {
            return 0;
        }
        try {
            long millis = Long.parseLong(durationText);
            return (int) (millis / 1000L);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
