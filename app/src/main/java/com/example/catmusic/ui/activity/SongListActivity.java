package com.example.catmusic.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.catmusic.Config;
import com.example.catmusic.R;
import com.example.catmusic.adapter.SongsRecyclerViewAdapter;
import com.example.catmusic.bean.SongUrls;
import com.example.catmusic.bean.SongsList;
import com.example.catmusic.utils.FavoriteManager;
import com.example.catmusic.utils.LocalMusicManager;
import com.example.catmusic.utils.LogUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 歌曲列表展示页面。
 * 同时展示远程专辑歌曲与用户长期保存的本地导入歌曲。
 */
public class SongListActivity extends BaseActivity {
    private static final String TAG = "SongListActivity";

    private ImageView ablumsIcon;
    private TextView ablumsTitle;
    private TextView importLocalMusicButton;
    private RecyclerView songsListView;
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SongsRecyclerViewAdapter songsAdapter;
    private final List<SongsList.ResultBean.SongsBean> songs = new ArrayList<>();
    private final List<SongsList.ResultBean.SongsBean> remoteSongs = new ArrayList<>();
    private FavoriteManager favoriteManager;
    private LocalMusicManager localMusicManager;

    private final ActivityResultLauncher<String[]> importAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), this::handleImportedAudioUris);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ablums_list);

        initTabBar(true, "专辑歌曲", false);
        initView();
        initOkHttp();
        setAlbumData();
        initSongsRecyclerAdapter();
        bindImportAction();
        rebuildSongsDisplay();
        getRecommendSongs();
    }

    private void initView() {
        ablumsIcon = fd(R.id.ablums_icon);
        ablumsTitle = fd(R.id.ablums_title);
        songsListView = fd(R.id.albums_list);
        importLocalMusicButton = fd(R.id.btn_import_local_music);
    }

    private void initOkHttp() {
        okHttpClient = new OkHttpClient.Builder().build();
        gson = new Gson();
        localMusicManager = new LocalMusicManager(this);
    }

    private void initSongsRecyclerAdapter() {
        songsListView.setLayoutManager(new LinearLayoutManager(this));
        favoriteManager = new FavoriteManager(this);
        songsAdapter = new SongsRecyclerViewAdapter(this, songs);
        songsAdapter.setFavoriteManager(favoriteManager);
        songsListView.setAdapter(songsAdapter);
    }

    private void bindImportAction() {
        if (importLocalMusicButton != null) {
            importLocalMusicButton.setOnClickListener(v -> importAudioLauncher.launch(new String[]{"audio/*"}));
        }
    }

    private void setAlbumData() {
        String pic = getIntent().getStringExtra("pic");
        String title = getIntent().getStringExtra("title");
        ablumsTitle.setText(title != null ? title : "专辑歌曲");
        Glide.with(this)
                .load(pic)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ablumsIcon);
    }

    private void getRecommendSongs() {
        long albumId = getIntent().getLongExtra("id", 0);
        String url = Config.BASE_URL + Config.API_GET_ALBUM + "?id=" + albumId;
        LogUtil.d(TAG, "请求专辑歌曲列表: " + url);
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, "获取歌曲列表失败: " + e.getMessage());
                runOnUiThread(() -> {
                    rebuildSongsDisplay();
                    showSafeToast("获取歌曲列表失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        SongsList songsList = gson.fromJson(jsonData, SongsList.class);
                        runOnUiThread(() -> handleSongsData(songsList));
                    } catch (Exception e) {
                        LogUtil.e(TAG, "解析歌曲数据失败: " + e.getMessage());
                        runOnUiThread(() -> showSafeToast("解析歌曲数据失败", Toast.LENGTH_SHORT));
                    }
                } else {
                    LogUtil.e(TAG, "服务器响应失败: " + response.code());
                    runOnUiThread(() -> showSafeToast("服务器响应失败: " + response.code(), Toast.LENGTH_SHORT));
                }
            }
        });
    }

    private void getSongsUrl() {
        if (remoteSongs.isEmpty()) {
            rebuildSongsDisplay();
            return;
        }

        StringBuilder midUrls = new StringBuilder();
        for (SongsList.ResultBean.SongsBean song : remoteSongs) {
            if (song == null || song.isLocal()) {
                continue;
            }
            String mid = song.getMid();
            if (mid == null || mid.isEmpty()) {
                continue;
            }
            if (midUrls.length() > 0) {
                midUrls.append("&");
            }
            midUrls.append("mid[]=").append(mid);
        }

        if (midUrls.length() == 0) {
            rebuildSongsDisplay();
            return;
        }

        String url = Config.BASE_URL + Config.API_GET_SONGS_URL + "?" + midUrls;
        LogUtil.d(TAG, "请求歌曲URL: " + url);
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, "获取歌曲URL失败: " + e.getMessage());
                runOnUiThread(() -> showSafeToast("获取歌曲URL失败: " + e.getMessage(), Toast.LENGTH_SHORT));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        SongUrls songUrls = gson.fromJson(jsonData, SongUrls.class);
                        runOnUiThread(() -> handleSongUrls(songUrls));
                    } catch (Exception e) {
                        LogUtil.e(TAG, "解析歌曲URL数据失败: " + e.getMessage());
                        runOnUiThread(() -> showSafeToast("解析歌曲URL数据失败", Toast.LENGTH_SHORT));
                    }
                } else {
                    LogUtil.e(TAG, "获取歌曲URL响应失败: " + response.code());
                    runOnUiThread(() -> showSafeToast("获取歌曲URL响应失败: " + response.code(), Toast.LENGTH_SHORT));
                }
            }
        });
    }

    private void handleSongUrls(SongUrls songUrls) {
        if (songUrls != null && songUrls.getCode() == 0 && songUrls.getResult() != null) {
            for (SongsList.ResultBean.SongsBean song : remoteSongs) {
                String mid = song.getMid();
                if (mid != null && songUrls.getResult().getMap().containsKey(mid)) {
                    String url = songUrls.getResult().getMap().get(mid);
                    if (url != null && !url.isEmpty()) {
                        song.setUrl(url);
                    }
                }
            }
            rebuildSongsDisplay();
        } else {
            LogUtil.e(TAG, "未获取到有效的歌曲URL数据，songUrls对象: " + songUrls);
            showSafeToast("未获取到有效的歌曲URL数据", Toast.LENGTH_SHORT);
        }
    }

    private void handleSongsData(SongsList songsListResponse) {
        remoteSongs.clear();
        if (songsListResponse != null && songsListResponse.getResult() != null) {
            List<SongsList.ResultBean.SongsBean> songData = songsListResponse.getResult().getSongs();
            if (songData != null) {
                remoteSongs.addAll(songData);
                showSafeToast("共获取到 " + songData.size() + " 首远程歌曲", Toast.LENGTH_SHORT);
            }
        }
        rebuildSongsDisplay();
        getSongsUrl();
    }

    private void rebuildSongsDisplay() {
        songs.clear();
        songs.addAll(remoteSongs);
        songs.addAll(localMusicManager.getAllImportedSongs());
        songsAdapter.notifyDataSetChanged();
    }

    private void handleImportedAudioUris(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        List<SongsList.ResultBean.SongsBean> importedSongs = new ArrayList<>();
        for (Uri audioUri : uris) {
            if (audioUri == null) {
                continue;
            }
            takePersistableReadPermission(audioUri);
            String lyricUri = findMatchingLyricUri(audioUri);
            SongsList.ResultBean.SongsBean song = localMusicManager.buildSongFromAudioUri(audioUri, lyricUri);
            if (song != null) {
                importedSongs.add(song);
            }
        }

        int updatedCount = localMusicManager.upsertImportedSongs(importedSongs);
        rebuildSongsDisplay();
        showSafeToast("已导入 " + updatedCount + " 首本地歌曲", Toast.LENGTH_SHORT);
    }

    private void takePersistableReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            LogUtil.w(TAG, "持久化读取权限失败: " + e.getMessage());
        }
    }

    private String findMatchingLyricUri(Uri audioUri) {
        String displayName = queryDisplayName(audioUri);
        if (displayName == null || !DocumentsContract.isDocumentUri(this, audioUri)) {
            return null;
        }

        int dotIndex = displayName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? displayName.substring(0, dotIndex) : displayName;
        String documentId;
        try {
            documentId = DocumentsContract.getDocumentId(audioUri);
        } catch (IllegalArgumentException e) {
            return null;
        }

        int separator = documentId.lastIndexOf('/');
        String parentPath = separator >= 0 ? documentId.substring(0, separator + 1) : "";
        for (String extension : new String[]{".lrc", ".txt"}) {
            Uri lyricUri = DocumentsContract.buildDocumentUri(
                    audioUri.getAuthority(),
                    parentPath + baseName + extension
            );
            if (canOpenUri(lyricUri)) {
                takePersistableReadPermission(lyricUri);
                return lyricUri.toString();
            }
        }
        return null;
    }

    private String queryDisplayName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception e) {
            LogUtil.w(TAG, "读取文件名失败: " + e.getMessage());
        }
        return null;
    }

    private boolean canOpenUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        try (InputStream ignored = getContentResolver().openInputStream(uri)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
