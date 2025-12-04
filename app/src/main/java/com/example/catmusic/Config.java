package com.example.catmusic;

/**
 * 应用配置类
 * 统一管理服务器地址和API接口
 */
public class Config {
    // 服务器基础URL
    public static final String BASE_URL = "http://172.18.16.222:3000/";// 服务器基础URL
    
    // API接口路径
    public static final String API_GET_RECOMMEND = "api/getRecommend";// 获取推荐歌单
    public static final String API_GET_SONGS_URL = "api/getSongsUrl";// 获取歌曲URL
    public static final String API_GET_ALBUM = "api/getAlbum";// 获取歌单专辑
    public static final String API_GET_LYRIC = "api/getLyric";// 获取歌词
    
    /**
     * 获取完整的API URL
     */
    public static String getApiUrl(String apiPath) {
        return BASE_URL + apiPath;
    }
}