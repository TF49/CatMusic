package com.example.catmusic.service;

/**
 * 播放状态枚举
 */
public enum PlaybackState {
    IDLE,      // 空闲状态
    PLAYING,   // 播放中
    PAUSED,    // 已暂停
    STOPPED,   // 已停止
    ERROR      // 错误状态
}