package com.example.catmusic.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.catmusic.R;
import com.example.catmusic.bean.SongUrls;
import com.example.catmusic.bean.SongsList;
import com.example.catmusic.service.MusicService;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlayerActivity extends BaseActivity implements MusicService.OnPlaybackStateChange {
    private static final String TAG = "PlayerActivity";

    private ImageView albumArt;
    private TextView songTitle;
    private TextView songArtist;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private ImageView loopButton;
    private ImageView prevButton;
    private ImageView playPauseButton;
    private ImageView nextButton;
    private ImageView randomButton;
    
    // 旋转动画相关
    private android.view.animation.RotateAnimation rotateAnimation;
    private boolean isRotating = false;
    
    // 歌词相关
    private com.example.catmusic.lyric.LyricView lyricView;
    private TextView noLyricText;
    private com.example.catmusic.biz.LyricBiz lyricBiz;
    private com.example.catmusic.bean.Lyric currentLyric;
    private Handler lyricHandler = new Handler();
    private Runnable updateLyricRunnable = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.isPlaying() && currentLyric != null) {
                int currentProgress = musicService.getCurrentProgress();
                lyricView.setCurrentTime(currentProgress);
                lyricHandler.postDelayed(this, 100); // 每100毫秒更新一次
            }
        }
    };

    private MusicService musicService;
    private boolean serviceBound = false;
    private List<SongsList.ResultBean.SongsBean> songsList = new ArrayList<>();
    private int currentPosition = 0;//当前播放歌曲的索引
    
    // 音频焦点管理
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    
    // 网络请求相关
    private OkHttpClient okHttpClient;
    private Gson gson;

    // 使用静态内部类和弱引用避免内存泄漏
    private static class SeekBarUpdateHandler extends Handler {
        private final WeakReference<PlayerActivity> activityRef;

        SeekBarUpdateHandler(PlayerActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            PlayerActivity activity = activityRef.get();
            if (activity != null && activity.musicService != null && activity.musicService.isPlaying()) {
                int currentPosition = activity.musicService.getCurrentProgress();
                int duration = activity.musicService.getDuration();
                activity.seekBar.setProgress(currentPosition);
                activity.currentTime.setText(activity.formatTime(currentPosition));
                
                // 更新总时间显示
                if (duration > 0 && !activity.totalTime.getText().toString().equals(activity.formatTime(duration))) {
                    activity.totalTime.setText(activity.formatTime(duration));
                    activity.seekBar.setMax(duration);
                }
                
                // 继续更新
                activity.handler.postDelayed(activity.updateSeekBarRunnable, 1000);
            }
        }
    }

    private Handler handler = new SeekBarUpdateHandler(this);
    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.isPlaying()) {
                int currentPosition = musicService.getCurrentProgress();
                int duration = musicService.getDuration();
                seekBar.setProgress(currentPosition);
                currentTime.setText(formatTime(currentPosition));
                
                // 更新总时间显示
                if (duration > 0 && !totalTime.getText().toString().equals(formatTime(duration))) {
                    totalTime.setText(formatTime(duration));
                    seekBar.setMax(duration);
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // 初始化导航栏
        initTabBar(true, "播放器", false);

        // 初始化网络请求组件
        initOkHttp();
        
        // 初始化界面组件
        initView();

        // 获取传递的数据
        handleIntentData();

        // 设置点击事件
        setClickListeners();

        // 启动并绑定服务
        Intent serviceIntent = new Intent(this, MusicService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // 初始化音频焦点管理
        initializeAudioFocus();

    }

    // 初始化网络请求相关的组件
    private void initOkHttp() {
        okHttpClient = new OkHttpClient.Builder().build();
        gson = new Gson();
    }
    
    // 初始化音频焦点管理
    private void initializeAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // 重新获得音频焦点，恢复播放
                        Log.d(TAG, "重新获得音频焦点");
                        if (musicService != null && musicService.isPaused()) {
                            musicService.resumeMusic();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // 永久失去音频焦点，暂停播放
                        Log.d(TAG, "永久失去音频焦点");
                        if (musicService != null && musicService.isPlaying()) {
                            musicService.pauseMusic();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // 暂时失去音频焦点，暂停播放
                        Log.d(TAG, "暂时失去音频焦点");
                        if (musicService != null && musicService.isPlaying()) {
                            musicService.pauseMusic();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // 暂时失去音频焦点，可以降低音量
                        Log.d(TAG, "暂时失去音频焦点，降低音量");
                        // 这里由MusicService处理音量调整
                        break;
                }
            }
        };
        
        // 请求音频焦点
        requestAudioFocus();
    }
    
    // 请求音频焦点
    private boolean requestAudioFocus() {
        if (audioManager != null) {
            int result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            );
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        return false;
    }
    
    // 放弃音频焦点
    private void abandonAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }
    
    // 初始化旋转动画
    private void initRotateAnimation() {
        // 创建旋转动画：从0度到360度，持续15秒，无限循环
        rotateAnimation = new android.view.animation.RotateAnimation(
                0f, 360f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(15000); // 15秒完成一圈，更缓慢流畅
        rotateAnimation.setRepeatCount(android.view.animation.Animation.INFINITE); // 无限循环
        rotateAnimation.setInterpolator(new android.view.animation.LinearInterpolator()); // 线性插值器，匀速旋转
        rotateAnimation.setFillAfter(true); // 动画结束后保持状态
        rotateAnimation.setFillEnabled(true); // 启用填充效果
    }
    
    // 初始化歌词相关组件
    private void initLyricComponents() {
        lyricView = findViewById(R.id.lyric_view);
        noLyricText = findViewById(R.id.no_lyric_text);
        lyricBiz = new com.example.catmusic.biz.LyricBiz();
        
        // 设置歌词视图的初始状态
        if (lyricView != null) {
            lyricView.setVisibility(View.VISIBLE);
        }
        if (noLyricText != null) {
            noLyricText.setVisibility(View.GONE);
        }
    }
    
    // 获取当前歌曲的歌词
    private void getCurrentSongLyric() {
        if (songsList.isEmpty() || currentPosition < 0 || currentPosition >= songsList.size()) {
            return;
        }
        
        SongsList.ResultBean.SongsBean song = songsList.get(currentPosition);
        if (song == null || song.getMid() == null || song.getMid().isEmpty()) {
            Log.w(TAG, "当前歌曲没有有效的MID，无法获取歌词");
            showNoLyric();
            return;
        }
        
        String mid = song.getMid();
        Log.d(TAG, "开始获取歌词，歌曲MID: " + mid);
        
        lyricBiz.getLyric(mid, new com.example.catmusic.biz.LyricBiz.LyricCallback() {
            @Override
            public void onSuccess(String lyricData) {
                Log.d(TAG, "获取歌词成功");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleLyricData(lyricData);
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "获取歌词失败: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNoLyric();
                        showSafeToast("获取歌词失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }
    
    // 处理歌词数据
    private void handleLyricData(String lyricData) {
        if (lyricData == null || lyricData.isEmpty()) {
            Log.w(TAG, "歌词数据为空");
            showNoLyric();
            return;
        }
        
        try {
            // 解析歌词
            currentLyric = com.example.catmusic.utils.LyricParser.parseLyric(lyricData);
            
            if (currentLyric == null || currentLyric.getLyricLines().isEmpty()) {
                Log.w(TAG, "解析歌词失败或歌词为空");
                showNoLyric();
                return;
            }
            
            // 设置歌词到视图
            if (lyricView != null) {
                lyricView.setLyric(currentLyric);
                lyricView.setVisibility(View.VISIBLE);
            }
            if (noLyricText != null) {
                noLyricText.setVisibility(View.GONE);
            }
            
            Log.d(TAG, "歌词设置成功，共 " + currentLyric.getLyricLines().size() + " 行歌词");
            
            // 开始歌词同步更新
            startLyricSync();
            
        } catch (Exception e) {
            Log.e(TAG, "处理歌词数据时出错: " + e.getMessage());
            showNoLyric();
        }
    }
    
    // 显示无歌词状态
    private void showNoLyric() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (lyricView != null) {
                    lyricView.setVisibility(View.GONE);
                }
                if (noLyricText != null) {
                    noLyricText.setVisibility(View.VISIBLE);
                }
                
                // 停止歌词同步
                stopLyricSync();
            }
        });
    }
    
    // 开始歌词同步
    private void startLyricSync() {
        lyricHandler.removeCallbacks(updateLyricRunnable);
        lyricHandler.post(updateLyricRunnable);
    }
    
    // 停止歌词同步
    private void stopLyricSync() {
        lyricHandler.removeCallbacks(updateLyricRunnable);
    }
    
    // 开始旋转动画
    private void startRotateAnimation() {
        if (albumArt != null && rotateAnimation != null && !isRotating) {
            albumArt.startAnimation(rotateAnimation);
            isRotating = true;
            Log.d(TAG, "开始专辑封面旋转动画");
        }
    }
    
    // 暂停旋转动画
    private void pauseRotateAnimation() {
        if (albumArt != null && isRotating) {
            // 获取当前动画的旋转角度
            android.view.animation.Animation currentAnimation = albumArt.getAnimation();
            if (currentAnimation != null) {
                currentAnimation.cancel();
            }
            isRotating = false;
            Log.d(TAG, "暂停专辑封面旋转动画");
        }
    }
    
    // 停止旋转动画
    private void stopRotateAnimation() {
        if (albumArt != null) {
            albumArt.clearAnimation();
            isRotating = false;
            Log.d(TAG, "停止专辑封面旋转动画");
        }
    }

    private void initView() {
        albumArt = findViewById(R.id.player_album_art);
        songTitle = findViewById(R.id.player_song_title);
        songArtist = findViewById(R.id.player_song_artist);
        currentTime = findViewById(R.id.player_current_time);
        totalTime = findViewById(R.id.player_total_time);
        seekBar = findViewById(R.id.player_seekbar);
        loopButton = findViewById(R.id.btn_repeat);
        prevButton = findViewById(R.id.btn_prev);
        playPauseButton = findViewById(R.id.btn_play_pause);
        nextButton = findViewById(R.id.btn_next);
        randomButton = findViewById(R.id.btn_shuffle);
        
        // 初始化旋转动画
        initRotateAnimation();
        
        // 初始化歌词相关组件
        initLyricComponents();

        // 设置SeekBar拖动监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 停止自动更新
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 恢复自动更新
                handler.post(updateSeekBarRunnable);
            }
        });
    }

    private void handleIntentData() {
        // 获取传递的歌曲列表和当前位置
        if (getIntent().hasExtra("songs_list")) {
            songsList = (ArrayList<SongsList.ResultBean.SongsBean>) getIntent().getSerializableExtra("songs_list");
        }

        if (getIntent().hasExtra("current_position")) {
            currentPosition = getIntent().getIntExtra("current_position", 0);
        }

        Log.d(TAG, "接收到歌曲列表，共 " + songsList.size() + " 首歌曲，当前播放第 " + currentPosition + " 首");

        // 获取歌曲播放URL
        getSongsUrl();
    }
    
    /**
     * 获取歌曲URL地址
     * <p>
     * 根据已有的歌曲 MID 列表构建查询参数，向服务端请求对应的播放地址，
     * 并在获取成功后更新本地歌曲对象中的 URL 字段。
     * </p>
     */
    private void getSongsUrl() {
        if (songsList == null || songsList.isEmpty()) {
            Log.w(TAG, "歌曲列表为空，无法获取URL");
            return;
        }

        try {
            // 构建mid参数：mid[]=0009Q7MT3WQKpB&mid[]=001maG3s4AJfuU&mid[]=004FgYOA33AR6H
            StringBuilder midUrls = new StringBuilder();
            for (int i = 0; i < songsList.size(); i++) {
                String mid = songsList.get(i).getMid();
                if (mid != null && !mid.isEmpty()) {
                    if (i == 0) {
                        midUrls.append("mid[]=").append(mid);
                    } else {
                        midUrls.append("&mid[]=").append(mid);
                    }
                }
            }

            // 检查是否有有效的MID
            if (midUrls.length() == 0) {
                Log.w(TAG, "没有有效的歌曲MID，无法获取URL");
                showSafeToast("没有有效的歌曲信息", Toast.LENGTH_SHORT);
                return;
            }

            // 修正URL地址，使用与歌曲API相同的服务器地址
            String url = "http://172.18.16.222:3000/api/getSongsUrl?" + midUrls.toString();
            Log.d(TAG, "请求歌曲URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "获取歌曲URL失败: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSafeToast("获取歌曲URL失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonData = response.body().string();
                            Log.d(TAG, "获取歌曲URL响应: " + jsonData);
                            SongUrls songUrls = gson.fromJson(jsonData, SongUrls.class);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleSongUrls(songUrls);
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "解析歌曲URL数据失败: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showSafeToast("解析歌曲URL数据失败", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "获取歌曲URL响应失败: " + response.code());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSafeToast("获取歌曲URL响应失败: " + response.code(), Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "构建歌曲URL请求时出错: " + e.getMessage());
            showSafeToast("请求构建失败: " + e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    /**
     * 处理歌曲URL数据
     *
     * @param songUrls 包含歌曲播放地址映射的对象
     */
    private void handleSongUrls(SongUrls songUrls) {
        if (songUrls != null && songUrls.getCode() == 0 && songUrls.getResult() != null) {
            int updatedCount = 0;

            // 更新每首歌曲的URL
            for (SongsList.ResultBean.SongsBean song : songsList) {
                String mid = song.getMid();
                if (mid != null && songUrls.getResult().getMap().containsKey(mid)) {
                    String url = songUrls.getResult().getMap().get(mid);
                    Log.d(TAG, "歌曲 MID: " + mid + ", URL: " + url);
                    if (url != null && !url.isEmpty()) {
                        song.setUrl(url); // 确保SongsBean中有setUrl方法
                        updatedCount++;
                    }
                }
            }
            
            showSafeToast("成功更新 " + updatedCount + " 首歌曲的播放地址", Toast.LENGTH_SHORT);
            
            // 更新界面显示
            updateSongInfo();
        } else {
            Log.e(TAG, "未获取到有效的歌曲URL数据，songUrls对象: " + songUrls);
            showSafeToast("未获取到有效的歌曲URL数据", Toast.LENGTH_SHORT);
        }
    }

    private void setClickListeners() {
        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 循环模式按钮
        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    int currentMode = musicService.getPlayMode();
                    // 按顺序切换模式: 列表循环 -> 单曲循环 -> 随机播放 -> 列表循环...
                    int newMode;
                    switch (currentMode) {
                        case MusicService.MODE_LOOP_ALL:
                            newMode = MusicService.MODE_LOOP_ONE;
                            break;
                        case MusicService.MODE_LOOP_ONE:
                            newMode = MusicService.MODE_RANDOM;
                            break;
                        case MusicService.MODE_RANDOM:
                            newMode = MusicService.MODE_LOOP_ALL;
                            break;
                        default:
                            newMode = MusicService.MODE_LOOP_ALL;
                    }
                    musicService.setPlayMode(newMode);
                    updateLoopButton(newMode);
                    // 如果当前是随机模式，也需要更新随机按钮的状态
                    updateRandomButton(newMode);
                }
            }
        });

        // 随机播放按钮
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    int currentMode = musicService.getPlayMode();
                    // 切换随机模式开关
                    int newMode = (currentMode == MusicService.MODE_RANDOM) ? 
                                  MusicService.MODE_LOOP_ALL : MusicService.MODE_RANDOM;
                    musicService.setPlayMode(newMode);
                    updateLoopButton(newMode);
                    updateRandomButton(newMode);
                }
            }
        });

        // 上一首按钮
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    musicService.playPrev();
                }
            }
        });

        // 下一首按钮
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    musicService.playNext();
                }
            }
        });

        // 播放/暂停按钮
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    if (musicService.isPlaying()) {
                        musicService.pauseMusic();
                    } else {
                        musicService.resumeMusic();
                    }
                }
            }
        });

    }

    private void updateSongInfo() {
        if (songsList.isEmpty() || currentPosition < 0 || currentPosition >= songsList.size()) {
            return;
        }

        SongsList.ResultBean.SongsBean song = songsList.get(currentPosition);

        // 更新歌曲信息
        songTitle.setText(song.getName() != null ? song.getName() : "未知歌曲");
        songArtist.setText(song.getSinger() != null ? song.getSinger() : "未知歌手");

        // 加载专辑封面
        if (song.getPic() != null && !song.getPic().isEmpty()) {
            Glide.with(this)
                    .load(song.getPic())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(albumArt);
        } else {
            albumArt.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 更新总时间
        if (song.getDuration() > 0) {
            totalTime.setText(formatTime(song.getDuration() * 1000)); // duration是秒，转换为毫秒
            seekBar.setMax(song.getDuration() * 1000);
        } else {
            totalTime.setText("00:00");
            seekBar.setMax(0);
        }

        currentTime.setText("00:00");
        seekBar.setProgress(0);
        
        // 获取当前歌曲的歌词
        getCurrentSongLyric();
    }

    private void updateLoopButton(int mode) {
        switch (mode) {
            case MusicService.MODE_LOOP_ALL:
                loopButton.setImageResource(R.drawable.loop);
                break;
            case MusicService.MODE_LOOP_ONE:
                loopButton.setImageResource(R.drawable.loop_active);
                break;
            case MusicService.MODE_RANDOM:
                loopButton.setImageResource(R.drawable.loop);
                break;
        }
    }

    private void updateRandomButton(int mode) {
        if (mode == MusicService.MODE_RANDOM) {
            randomButton.setImageResource(R.drawable.random_active);
        } else {
            randomButton.setImageResource(R.drawable.random);
        }
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "服务连接成功");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;

            // 设置歌曲列表和当前位置
            if (musicService != null) {
                musicService.setSongsList(songsList);
                musicService.setCurrentPosition(currentPosition);
                musicService.setOnPlaybackStateChange(PlayerActivity.this);
                musicService.playMusic();

                // 更新播放按钮状态
                playPauseButton.setImageResource(R.drawable.pause_active);

                // 开始更新进度条
                handler.post(updateSeekBarRunnable);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "服务断开连接");
            serviceBound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // 在Activity恢复时重新请求音频焦点
        if (audioManager != null) {
            requestAudioFocus();
        }
        
        // 如果服务已绑定，更新界面
        if (serviceBound && musicService != null) {
            // 更新播放状态
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.pause_active);
            } else {
                playPauseButton.setImageResource(R.drawable.play_active);
            }

            // 更新播放模式按钮
            updateLoopButton(musicService.getPlayMode());
            updateRandomButton(musicService.getPlayMode());

            // 更新歌曲信息
            updateSongInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在Activity暂停时放弃音频焦点
        abandonAudioFocus();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PlayerActivity onDestroy");
        
        // 停止旋转动画
        stopRotateAnimation();
        
        // 停止歌词同步
        stopLyricSync();
        
        // 停止进度条更新
        handler.removeCallbacks(updateSeekBarRunnable);
        
        // 解绑服务
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        // 放弃音频焦点
        abandonAudioFocus();
    }

    // MusicService.OnPlaybackStateChange 接口实现
    @Override
    public void onPlay() {
        Log.d(TAG, "收到播放事件");
        try {
            if (playPauseButton != null) {
                playPauseButton.setImageResource(R.drawable.pause_active);
            }
            
            // 开始旋转动画
            startRotateAnimation();
            
            // 确保进度条更新开始
            handler.removeCallbacks(updateSeekBarRunnable);
            handler.post(updateSeekBarRunnable);
            
            // 开始歌词同步
            startLyricSync();
        } catch (Exception e) {
            Log.e(TAG, "处理播放事件时出错: " + e.getMessage());
        }
    }

    @Override
    public void onPlaybackPause() {
        Log.d(TAG, "收到暂停事件");
        try {
            if (playPauseButton != null) {
                playPauseButton.setImageResource(R.drawable.play_active);
            }
            
            // 暂停旋转动画
            pauseRotateAnimation();
            
            // 停止进度条更新
            handler.removeCallbacks(updateSeekBarRunnable);
            
            // 停止歌词同步
            stopLyricSync();
        } catch (Exception e) {
            Log.e(TAG, "处理暂停事件时出错: " + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();// 调用父类的onStop方法
        Log.d(TAG, "收到停止事件");
        try {
            if (playPauseButton != null) {
                playPauseButton.setImageResource(R.drawable.play_active);
            }
            if (seekBar != null) {
                seekBar.setProgress(0);
            }
            if (currentTime != null) {
                currentTime.setText("00:00");
            }
            
            // 停止旋转动画
            stopRotateAnimation();
            
            // 停止进度条更新
            handler.removeCallbacks(updateSeekBarRunnable);
            
            // 停止歌词同步
            stopLyricSync();
        } catch (Exception e) {
            Log.e(TAG, "处理停止事件时出错: " + e.getMessage());
        }
    }

    @Override
    public void onCompletion() {
        Log.d(TAG, "收到播放完成事件");
        try {
            // 播放完成时更新界面
            updateSongInfo();
            
            // 停止旋转动画
            stopRotateAnimation();
            
            // 确保进度条更新停止
            handler.removeCallbacks(updateSeekBarRunnable);
        } catch (Exception e) {
            Log.e(TAG, "处理播放完成事件时出错: " + e.getMessage());
        }
    }

    @Override
    public void onSongChanged(SongsList.ResultBean.SongsBean song, int position) {
        Log.d(TAG, "收到歌曲变更事件: " + song.getName() + ", 位置: " + position);
        try {
            currentPosition = position;
            updateSongInfo();
            
            // 重新开始旋转动画
            stopRotateAnimation();
            startRotateAnimation();
            
            // 重新开始进度条更新
            handler.removeCallbacks(updateSeekBarRunnable);
            handler.post(updateSeekBarRunnable);
            
            // 获取新歌曲的歌词
            getCurrentSongLyric();
        } catch (Exception e) {
            Log.e(TAG, "处理歌曲变更事件时出错: " + e.getMessage());
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "收到错误事件: " + error);
        try {
            showSafeToast("播放错误: " + error, Toast.LENGTH_SHORT);
            
            // 停止进度条更新
            handler.removeCallbacks(updateSeekBarRunnable);
        } catch (Exception e) {
            Log.e(TAG, "处理错误事件时出错: " + e.getMessage());
        }
    }
}