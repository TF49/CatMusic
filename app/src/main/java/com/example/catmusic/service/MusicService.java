package com.example.catmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.catmusic.R;
import com.example.catmusic.bean.SongsList;
import com.example.catmusic.service.PlaybackState;
import com.example.catmusic.ui.activity.PlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "MusicService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "music_channel";

    // 播放模式常量
    public static final int MODE_LOOP_ALL = 0;  // 列表循环
    public static final int MODE_LOOP_ONE = 1;  // 单曲循环
    public static final int MODE_RANDOM = 2;    // 随机播放

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private List<SongsList.ResultBean.SongsBean> songsList = new ArrayList<>();//播放列表
    private int currentPosition = 0;// 当前播放歌曲的索引
    private int playMode = MODE_LOOP_ALL;//播放模式
    private boolean isPaused = false;
    
    // 添加播放状态变量
    private PlaybackState playbackState = PlaybackState.IDLE;
    
    // 音频焦点管理
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    // 回调接口，用于通知Activity播放状态变化
    private OnPlaybackStateChange onPlaybackStateChange;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MusicService onCreate");
        createNotificationChannel();
        initializeMediaPlayer();
        initializeAudioFocus();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "MusicService onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "MusicService onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MusicService onStartCommand");
        
        // 处理通知按钮点击事件
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "ACTION_PREV":
                    playPrev();
                    break;
                case "ACTION_PLAY_PAUSE":
                    if (isPlaying()) {
                        pauseMusic();
                    } else {
                        resumeMusic();
                    }
                    // 更新通知以反映播放状态变化
                    updateNotification();
                    break;
                case "ACTION_NEXT":
                    playNext();
                    break;
            }
        }
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY; // 服务被杀死后会尝试重启
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MusicService onDestroy");
        stopForeground(true);
        
        // 放弃音频焦点
        abandonAudioFocus();
        
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * 创建通知渠道（Android 8.0及以上）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music Channel";
            String description = "Music playback notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification() {
        try {
            // 创建打开PlayerActivity的意图
            Intent notificationIntent = new Intent(this, PlayerActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            // 创建上一首按钮的意图
            Intent prevIntent = new Intent(this, MusicService.class);
            prevIntent.setAction("ACTION_PREV");
            PendingIntent prevPendingIntent = PendingIntent.getService(
                    this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);

            // 创建播放/暂停按钮的意图
            Intent playPauseIntent = new Intent(this, MusicService.class);
            playPauseIntent.setAction("ACTION_PLAY_PAUSE");
            PendingIntent playPausePendingIntent = PendingIntent.getService(
                    this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE);

            // 创建下一首按钮的意图
            Intent nextIntent = new Intent(this, MusicService.class);
            nextIntent.setAction("ACTION_NEXT");
            PendingIntent nextPendingIntent = PendingIntent.getService(
                    this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);

            // 创建自定义通知布局
            RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
            
            // 设置通知内容
            if (!songsList.isEmpty() && currentPosition < songsList.size()) {
                SongsList.ResultBean.SongsBean currentSong = songsList.get(currentPosition);
                notificationLayout.setTextViewText(R.id.notification_song_title, 
                    currentSong.getName() != null ? currentSong.getName() : "未知歌曲");
                notificationLayout.setTextViewText(R.id.notification_song_artist, 
                    currentSong.getSinger() != null ? currentSong.getSinger() : "未知歌手");
            }

            // 设置控制按钮的点击事件
            notificationLayout.setOnClickPendingIntent(R.id.notification_prev, prevPendingIntent);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, playPausePendingIntent);
            notificationLayout.setOnClickPendingIntent(R.id.notification_next, nextPendingIntent);

            // 根据播放状态设置播放/暂停按钮图标
            if (isPlaying()) {
                notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.pause_active);
            } else {
                notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.play_active);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("CatMusic")
                    .setContentText("正在播放音乐")
                    .setContentIntent(pendingIntent)
                    .setCustomContentView(notificationLayout)
                    .setOngoing(true) // 设置为持续通知
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "创建通知时出错: " + e.getMessage());
            // 返回一个简单的通知作为后备
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("CatMusic")
                    .setContentText("正在播放音乐")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
        }
    }

    /**
     * 初始化音频焦点管理
     */
    private void initializeAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // 重新获得音频焦点，恢复播放
                        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isPaused) {
                            mediaPlayer.start();
                            isPaused = false;
                            playbackState = PlaybackState.PLAYING;
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // 永久失去音频焦点，停止播放
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            isPaused = true;
                            playbackState = PlaybackState.PAUSED;
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // 暂时失去音频焦点，暂停播放
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            isPaused = true;
                            playbackState = PlaybackState.PAUSED;
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // 暂时失去音频焦点，可以降低音量
                        if (mediaPlayer != null) {
                            mediaPlayer.setVolume(0.2f, 0.2f);
                        }
                        break;
                }
            }
        };
    }

    /**
     * 请求音频焦点
     */
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

    /**
     * 放弃音频焦点
     */
    private void abandonAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    /**
     * 初始化MediaPlayer
     */
    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            
            // 设置音频流类型为音乐
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            
            // 设置音频属性
            mediaPlayer.setVolume(1.0f, 1.0f);
            
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
    }

    /**
     * 设置歌曲列表
     */
    public void setSongsList(List<SongsList.ResultBean.SongsBean> songsList) {
        this.songsList = songsList != null ? songsList : new ArrayList<>();
        Log.d(TAG, "设置歌曲列表，共 " + this.songsList.size() + " 首歌曲");
    }

    /**
     * 设置当前播放位置
     */
    public void setCurrentPosition(int position) {
        if (position >= 0 && position < songsList.size()) {
            this.currentPosition = position;
            Log.d(TAG, "设置当前播放位置: " + position);
        }
    }

    /**
     * 播放音乐
     */
    public void playMusic() {
        // 更新播放状态
        playbackState = PlaybackState.PLAYING;
        
        if (songsList.isEmpty()) {
            Log.e(TAG, "歌曲列表为空，无法播放");
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("歌曲列表为空，无法播放");
            }
            return;
        }

        if (currentPosition < 0 || currentPosition >= songsList.size()) {
            Log.e(TAG, "当前播放位置无效: " + currentPosition);
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("当前播放位置无效: " + currentPosition);
            }
            return;
        }

        SongsList.ResultBean.SongsBean song = songsList.get(currentPosition);
        String url = song.getUrl();

        if (url == null || url.isEmpty()) {
            Log.e(TAG, "歌曲URL为空，无法播放");
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("歌曲URL为空，无法播放");
            }
            return;
        }

        try {
            Log.d(TAG, "开始播放歌曲: " + song.getName() + ", URL: " + url);
            
            // 请求音频焦点
            if (!requestAudioFocus()) {
                Log.w(TAG, "无法获取音频焦点");
            }
            
            // 重置MediaPlayer
            if (mediaPlayer == null) {
                initializeMediaPlayer();
            }
            
            mediaPlayer.reset();
            
            // 设置数据源
            mediaPlayer.setDataSource(url);
            
            // 异步准备
            mediaPlayer.prepareAsync();
            
            // 更新通知
            updateNotification();
            
            // 通知Activity播放状态变化
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onSongChanged(song, currentPosition);
            }
        } catch (IOException e) {
            Log.e(TAG, "播放音乐时出错: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("播放音乐时出错: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer状态错误: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("MediaPlayer状态错误: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "未知错误: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("未知错误: " + e.getMessage());
            }
        }
    }

    /**
     * 暂停音乐
     */
    public void pauseMusic() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPaused = true;
                playbackState = PlaybackState.PAUSED;
                Log.d(TAG, "音乐已暂停");
                
                // 更新通知
                updateNotification();
                
                // 通知Activity播放状态变化
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onPlaybackPause();
            }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "暂停音乐时出错: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("暂停音乐时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 继续播放音乐
     */
    public void resumeMusic() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                isPaused = false;
                playbackState = PlaybackState.PLAYING;
                Log.d(TAG, "音乐继续播放");
                
                // 更新通知
                updateNotification();
                
                // 通知Activity播放状态变化
                if (onPlaybackStateChange != null) {
                    onPlaybackStateChange.onPlay();
                }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "继续播放音乐时出错: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("继续播放音乐时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 停止音乐
     */
    public void stopMusic() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                isPaused = false;
                playbackState = PlaybackState.STOPPED;
                Log.d(TAG, "音乐已停止");
                
                // 更新通知
                updateNotification();
                
                // 通知Activity播放状态变化
                if (onPlaybackStateChange != null) {
                    onPlaybackStateChange.onStop();
                }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "停止音乐时出错: " + e.getMessage());
            playbackState = PlaybackState.ERROR;
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("停止音乐时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        if (songsList.isEmpty()) {
            Log.w(TAG, "歌曲列表为空，无法播放下一首");
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("歌曲列表为空，无法播放下一首");
            }
            return;
        }

        try {
            int previousPosition = currentPosition;
            
            switch (playMode) {
                case MODE_LOOP_ALL:
                    currentPosition = (currentPosition + 1) % songsList.size();
                    Log.d(TAG, "列表循环模式，下一首位置: " + currentPosition);
                    break;
                case MODE_LOOP_ONE:
                    // 单曲循环，保持当前位置不变
                    Log.d(TAG, "单曲循环模式，保持当前位置: " + currentPosition);
                    break;
                case MODE_RANDOM:
                    int newPosition = new Random().nextInt(songsList.size());
                    currentPosition = newPosition;
                    Log.d(TAG, "随机播放模式，随机位置: " + currentPosition);
                    break;
                default:
                    currentPosition = (currentPosition + 1) % songsList.size();
                    Log.d(TAG, "默认列表循环模式，下一首位置: " + currentPosition);
                    break;
            }

            Log.d(TAG, "播放下一首，位置: " + currentPosition + " (之前: " + previousPosition + ")");
            
            // 如果位置改变或处于单曲循环模式，则播放新歌曲
            if (currentPosition != previousPosition || playMode == MODE_LOOP_ONE) {
                playMusic();
            } else {
                // 位置未改变，仍然需要通知界面更新
                if (onPlaybackStateChange != null) {
                    onPlaybackStateChange.onSongChanged(songsList.get(currentPosition), currentPosition);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "播放下一首时出错: " + e.getMessage());
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("播放下一首时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 播放上一首
     */
    public void playPrev() {
        if (songsList.isEmpty()) {
            Log.w(TAG, "歌曲列表为空，无法播放上一首");
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("歌曲列表为空，无法播放上一首");
            }
            return;
        }

        try {
            int previousPosition = currentPosition;
            
            switch (playMode) {
                case MODE_LOOP_ALL:
                    currentPosition = (currentPosition - 1 + songsList.size()) % songsList.size();
                    Log.d(TAG, "列表循环模式，上一首位置: " + currentPosition);
                    break;
                case MODE_LOOP_ONE:
                    // 单曲循环，保持当前位置不变
                    Log.d(TAG, "单曲循环模式，保持当前位置: " + currentPosition);
                    break;
                case MODE_RANDOM:
                    int newPosition = new Random().nextInt(songsList.size());
                    currentPosition = newPosition;
                    Log.d(TAG, "随机播放模式，随机位置: " + currentPosition);
                    break;
                default:
                    currentPosition = (currentPosition - 1 + songsList.size()) % songsList.size();
                    Log.d(TAG, "默认列表循环模式，上一首位置: " + currentPosition);
                    break;
            }

            Log.d(TAG, "播放上一首，位置: " + currentPosition + " (之前: " + previousPosition + ")");
            
            // 如果位置改变或处于单曲循环模式，则播放新歌曲
            if (currentPosition != previousPosition || playMode == MODE_LOOP_ONE) {
                playMusic();
            } else {
                // 位置未改变，仍然需要通知界面更新
                if (onPlaybackStateChange != null) {
                    onPlaybackStateChange.onSongChanged(songsList.get(currentPosition), currentPosition);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "播放上一首时出错: " + e.getMessage());
            if (onPlaybackStateChange != null) {
                onPlaybackStateChange.onError("播放上一首时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 跳转到指定位置播放
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(int mode) {
        if (mode >= MODE_LOOP_ALL && mode <= MODE_RANDOM) {
            this.playMode = mode;
            Log.d(TAG, "设置播放模式: " + mode);
        }
    }

    /**
     * 获取当前播放模式
     */
    public int getPlayMode() {
        return playMode;
    }

    /**
     * 获取当前播放进度
     */
    public int getCurrentProgress() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取歌曲总时长
     */
    public int getDuration() {
        if (mediaPlayer != null && mediaPlayer.getDuration() > 0) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 判断是否正在播放
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 判断是否已暂停
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 获取歌曲列表
     */
    public List<SongsList.ResultBean.SongsBean> getSongsList() {
        return songsList;
    }

    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * 设置播放状态变化回调
     */
    public void setOnPlaybackStateChange(OnPlaybackStateChange listener) {
        this.onPlaybackStateChange = listener;
    }

    /**
     * 更新通知
     */
    private void updateNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        } catch (Exception e) {
            Log.e(TAG, "更新通知时出错: " + e.getMessage());
        }
    }

    // MediaPlayer.OnPreparedListener 实现
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "MediaPlayer准备完成，开始播放");
        mediaPlayer.start();
        isPaused = false;
        playbackState = PlaybackState.PLAYING;
        
        // 通知Activity播放状态变化
        if (onPlaybackStateChange != null) {
            onPlaybackStateChange.onPlay();
        }
    }

    // MediaPlayer.OnCompletionListener 实现
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "歌曲播放完成");
        playbackState = PlaybackState.IDLE;
        
        // 根据播放模式处理播放完成事件
        switch (playMode) {
            case MODE_LOOP_ONE:
                // 单曲循环，重新播放当前歌曲
                playMusic();
                break;
            case MODE_LOOP_ALL:
            case MODE_RANDOM:
                // 列表循环或随机播放，播放下一首
                playNext();
                break;
        }
        
        // 通知Activity播放完成
        if (onPlaybackStateChange != null) {
            onPlaybackStateChange.onCompletion();
        }
    }

    // MediaPlayer.OnErrorListener 实现
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer错误 - what: " + what + ", extra: " + extra);
        playbackState = PlaybackState.ERROR;
        
        // 通知Activity发生错误
        if (onPlaybackStateChange != null) {
            onPlaybackStateChange.onError("播放错误: what=" + what + ", extra=" + extra);
        }
        
        return true; // 表示错误已处理
    }

    /**
     * 获取当前播放状态
     */
    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    /**
     * 播放状态变化回调接口
     */
    public interface OnPlaybackStateChange {
        void onPlay();
        void onPlaybackPause();
        void onStop();
        void onCompletion();
        void onSongChanged(SongsList.ResultBean.SongsBean song, int position);
        void onError(String error);
    }

    /**
     * 播放状态枚举
     */
    public enum PlaybackState {
        IDLE,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR
    }
}