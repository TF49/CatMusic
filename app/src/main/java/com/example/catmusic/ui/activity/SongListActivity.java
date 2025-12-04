package com.example.catmusic.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.catmusic.R;
import com.example.catmusic.adapter.SongsRecyclerViewAdapter;
import com.example.catmusic.bean.SongUrls;
import com.example.catmusic.bean.SongsList;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 歌曲列表展示页面
 * <p>
 * 该 Activity 负责展示某个专辑的详细信息，并通过网络请求获取并显示该专辑下的歌曲列表。
 * 页面初始化时会加载传入的专辑封面、标题等基本信息，并发起网络请求获取歌曲数据。
 * </p>
 */
public class SongListActivity extends BaseActivity
{
    private ImageView ablumsIcon;       // 专辑封面图片控件
    private TextView ablumsTitle;       // 专辑标题文本控件
    private RecyclerView songsList;     // 歌曲列表控件
    private OkHttpClient okHttpClient;  // 网络请求客户端
    private Gson gson;                  // JSON 解析工具
    private SongsRecyclerViewAdapter songsAdapter; // 歌曲列表适配器
    private List<SongsList.ResultBean.SongsBean> songs = new ArrayList<>(); // 歌曲数据列表

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ablums_list); // 使用 ablums_list 布局文件

        // 初始化导航栏
        initTabBar(true, "专辑歌曲", false);

        // 初始化界面组件
        initView();

        // 初始化网络请求组件
        initOkHttp();

        // 设置专辑数据
        setAlbumData();

        // 初始化歌曲列表适配器
        initSongsRecyclerAdapter();

        // 获取推荐歌曲数据
        getRecommendSongs();
    }

    /**
     * 初始化界面组件
     * <p>绑定布局中的 ImageView 和 TextView 控件</p>
     */
    private void initView()
    {
        ablumsIcon = fd(R.id.ablums_icon);
        ablumsTitle = fd(R.id.ablums_title);
        songsList = fd(R.id.albums_list); // 使用 albums_list ID
    }

    /**
     * 初始化网络请求相关的组件
     * <p>创建 OkHttpClient 实例用于发起网络请求，同时初始化 Gson 用于解析 JSON 数据</p>
     */
    private void initOkHttp()
    {
        okHttpClient = new OkHttpClient.Builder().build();
        gson = new Gson();
    }

    /**
     * 初始化歌曲列表适配器
     */
    private void initSongsRecyclerAdapter()
    {
        // 设置列表布局方式（垂直列表）
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        songsList.setLayoutManager(linearLayoutManager);

        // 创建适配器，传入空的歌曲列表
        songsAdapter = new SongsRecyclerViewAdapter(this, songs);
        songsList.setAdapter(songsAdapter);

        // 为适配器添加点击监听器
        // 设置点击事件：点击歌曲项时显示URL
        songsAdapter.setOnItemClickListener(new SongsRecyclerViewAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(int position)
            {
                // 点击事件现在在适配器内部处理，这里留空
            }
        });
    }

    /**
     * 设置专辑数据
     * <p>从 Intent 中提取专辑 ID、封面图 URL 和标题，并更新 UI 显示</p>
     */
    private void setAlbumData()
    {
        // 获取传递的专辑ID
        long albumId = getIntent().getLongExtra("id", 0);
        String pic = getIntent().getStringExtra("pic");
        String title = getIntent().getStringExtra("title");

        ablumsTitle.setText(title);
        Glide.with(this).load(pic).into(ablumsIcon);
        showSafeToast("专辑ID: " + albumId, Toast.LENGTH_SHORT);

        // 这里可以根据albumId加载专辑详情数据
    }

    /**
     * 从服务器获取专辑歌曲数据
     * <p>
     * 构造一个 HTTP 请求访问歌曲列表接口，使用 OkHttp 异步方式发送请求。
     * 在 onFailure 回调中处理请求失败情况，在 onResponse 回调中解析响应数据并更新 UI。
     * </p>
     */
    private void getRecommendSongs()
    {
        long albumId = getIntent().getLongExtra("id", 0);
        // 修复URL构造，正确拼接参数
        String url = "http://172.18.16.222:3000/api/getAlbum?id=" + albumId;
        Log.d("SongListActivity", "请求专辑歌曲列表: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 通过异步的方式发送请求
        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                Log.e("SongListActivity", "获取歌曲列表失败: " + e.getMessage());
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showSafeToast("获取歌曲列表失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                // 处理response响应对象，服务器返回的数据就放在这个对象上
                if (response.isSuccessful() && response.body() != null)
                {
                    try {
                        String jsonData = response.body().string();
                        Log.d("SongListActivity", "获取歌曲列表响应: " + jsonData);
                        // 将jsonData（json字符串）转换成歌曲列表对象
                        SongsList songsList = gson.fromJson(jsonData, SongsList.class);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // 处理歌曲数据
                                handleSongsData(songsList);
                                showSafeToast("获取歌曲列表成功", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.e("SongListActivity", "解析歌曲数据失败: " + e.getMessage());
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showSafeToast("解析歌曲数据失败", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }
                else
                {
                    // 添加服务器响应失败的处理
                    Log.e("SongListActivity", "服务器响应失败: " + response.code());
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showSafeToast("服务器响应失败: " + response.code(), Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
    }

    /**
     * 获取歌曲URL地址
     * <p>
     * 根据已有的歌曲 MID 列表构建查询参数，向服务端请求对应的播放地址，
     * 并在获取成功后更新本地歌曲对象中的 URL 字段。
     * </p>
     */
    private void getSongsUrl()
    {
        if (songs == null || songs.isEmpty())
        {
            return;
        }

        // 构建mid参数：mid[]=0009Q7MT3WQKpB&mid[]=001maG3s4AJfuU&mid[]=004FgYOA33AR6H
        StringBuilder midUrls = new StringBuilder();
        for (int i = 0; i < songs.size(); i++)
        {
            String mid = songs.get(i).getMid();
            if (mid != null && !mid.isEmpty())
            {
                if (i == 0)
                {
                    midUrls.append("mid[]=").append(mid);
                }
                else
                {
                    midUrls.append("&mid[]=").append(mid);
                }
            }
        }

        // 修正URL地址，使用与歌曲API相同的服务器地址
        String url = "http://172.18.16.222:3000/api/getSongsUrl?" + midUrls.toString();
        Log.d("SongListActivity", "请求歌曲URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                Log.e("SongListActivity", "获取歌曲URL失败: " + e.getMessage());
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showSafeToast("获取歌曲URL失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    try
                    {
                        String jsonData = response.body().string();
                        Log.d("SongListActivity", "获取歌曲URL响应: " + jsonData);
                        SongUrls songUrls = gson.fromJson(jsonData, SongUrls.class);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                handleSongUrls(songUrls);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.e("SongListActivity", "解析歌曲URL数据失败: " + e.getMessage());
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showSafeToast("解析歌曲URL数据失败", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }
                else
                {
                    Log.e("SongListActivity", "获取歌曲URL响应失败: " + response.code());
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showSafeToast("获取歌曲URL响应失败: " + response.code(), Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
    }

    /**
     * 处理歌曲URL数据
     *
     * @param songUrls 包含歌曲播放地址映射的对象
     */
    private void handleSongUrls(SongUrls songUrls)
    {
        if (songUrls != null && songUrls.getCode() == 0 && songUrls.getResult() != null)
        {
            int updatedCount = 0;

            // 更新每首歌曲的URL
            for (SongsList.ResultBean.SongsBean song : songs)
            {
                String mid = song.getMid();
                if (mid != null && songUrls.getResult().getMap().containsKey(mid))
                {
                    String url = songUrls.getResult().getMap().get(mid);
                    Log.d("SongListActivity", "歌曲 MID: " + mid + ", URL: " + url);
                    if (url != null && !url.isEmpty())
                    {
                        song.setUrl(url); // 确保SongsBean中有setUrl方法
                        updatedCount++;
                    }
                }
            }
            // 刷新适配器
            songsAdapter.notifyDataSetChanged();

            showSafeToast("成功更新 " + updatedCount + " 首歌曲的播放地址", Toast.LENGTH_SHORT);
        }
        else
        {
            Log.e("SongListActivity", "未获取到有效的歌曲URL数据，songUrls对象: " + songUrls);
            showSafeToast("未获取到有效的歌曲URL数据", Toast.LENGTH_SHORT);
        }
    }


    /**
     * 处理歌曲数据
     *
     * @param songsList 歌曲数据对象，类型应与 SongsList 类一致
     */
    private void handleSongsData(SongsList songsList)
    {
        // 处理歌曲数据，当数据有效时显示歌曲数量提示
        if (songsList != null && songsList.getResult() != null)
        {
            // 更新歌曲列表数据
            List<SongsList.ResultBean.SongsBean> songData = songsList.getResult().getSongs();
            if (songData != null)
            {
                songs.clear();
                songs.addAll(songData);
                songsAdapter.notifyDataSetChanged();

                showSafeToast("共获取到 " + songData.size() + " 首歌曲", Toast.LENGTH_SHORT);

                // 在获取到歌曲列表后，获取歌曲的播放URL
                getSongsUrl();
            }
        }
    }
}