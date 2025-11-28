package com.example.catmusic.bean;

import java.io.Serializable;
import java.util.List;

public class SongsList
{

    /**
     * code : 0
     * result : {"songs":[{"id":515011746,"mid":"0009Q7MT3WQKpB","name":"斯德哥尔摩的约会","singer":"赵永恒","url":"","duration":230,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000001ngxPw4Polma.jpg?max_age=2592000","album":"梦境悬空"},{"id":576590863,"mid":"001maG3s4AJfuU","name":"月亮偷偷告诉我","singer":"阮俊霖","url":"","duration":163,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M0000027gOYK3QEfgh.jpg?max_age=2592000","album":"月亮偷偷告诉我"},{"id":407046,"mid":"004FgYOA33AR6H","name":"日不落","singer":"蔡依林","url":"","duration":228,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000001tQgfA2o2Nra.jpg?max_age=2592000","album":"特务J"},{"id":4950137,"mid":"000qdZ603A8Nja","name":"飘摇","singer":"周迅","url":"","duration":247,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000001BZhvi3d0A1g.jpg?max_age=2592000","album":"亚洲新生开学了"},{"id":103754542,"mid":"003xcdD80qLLgh","name":"不再联系","singer":"夏天Alex","url":"","duration":204,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M0000021K0Vj4S5pAQ.jpg?max_age=2592000","album":"分手信"},{"id":107093125,"mid":"0036i2vr31cLQv","name":"棉花糖","singer":"至上励合","url":"","duration":228,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M0000012STFO3j8NrK.jpg?max_age=2592000","album":"降临"},{"id":168085,"mid":"00267qnK1qlUJB","name":"心悸","singer":"刘亦菲","url":"","duration":216,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000000hsxyu401TKc.jpg?max_age=2592000","album":"刘亦菲 首张国语专辑"},{"id":169045,"mid":"004L70aA4KEpW7","name":"放飞美丽","singer":"刘亦菲","url":"","duration":271,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000000hsxyu401TKc.jpg?max_age=2592000","album":"刘亦菲 首张国语专辑"},{"id":242776999,"mid":"003NiITr3dRjcn","name":"擦肩而过","singer":"宇桐非/胡雯","url":"","duration":204,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000000l0TLG3DjUT8.jpg?max_age=2592000","album":"与你同飞"},{"id":4758516,"mid":"001wXiwd0eRSes","name":"屋顶","singer":"周杰伦/温岚","url":"","duration":319,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000002GJDhP0ZluDv.jpg?max_age=2592000","album":"K情歌10"},{"id":104517,"mid":"004bSZTO2SUVzP","name":"舞娘","singer":"蔡依林","url":"","duration":184,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000001MFy5n2wnjDJ.jpg?max_age=2592000","album":"舞娘"},{"id":125431360,"mid":"000XfbFz2hbCmQ","name":"小情歌","singer":"苏打绿","url":"","duration":252,"pic":"https://y.gtimg.cn/mediastyle/music_v11/extra/default_300x300.jpg?max_age=31536000","album":""},{"id":405404,"mid":"003E1XgJ3RyycK","name":"特务J","singer":"蔡依林","url":"","duration":216,"pic":"https://y.gtimg.cn/music/photo_new/T002R800x800M000001tQgfA2o2Nra.jpg?max_age=2592000","album":"特务J"}]}
     */

    private int code;
    private ResultBean result;

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public ResultBean getResult()
    {
        return result;
    }

    public void setResult(ResultBean result)
    {
        this.result = result;
    }

    public static class ResultBean
    {
        private List<SongsBean> songs;

        public List<SongsBean> getSongs()
        {
            return songs;
        }

        public void setSongs(List<SongsBean> songs)
        {
            this.songs = songs;
        }

        public static class SongsBean implements Serializable
        {
            /**
             * id : 515011746
             * mid : 0009Q7MT3WQKpB
             * name : 斯德哥尔摩的约会
             * singer : 赵永恒
             * url :
             * duration : 230
             * pic : https://y.gtimg.cn/music/photo_new/T002R800x800M000001ngxPw4Polma.jpg?max_age=2592000
             * album : 梦境悬空
             */

            private int id;
            private String mid;
            private String name;
            private String singer;
            private String url;
            private int duration;
            private String pic;
            private String album;

            public int getId()
            {
                return id;
            }

            public void setId(int id)
            {
                this.id = id;
            }

            public String getMid()
            {
                return mid;
            }

            public void setMid(String mid)
            {
                this.mid = mid;
            }

            public String getName()
            {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
            }

            public String getSinger()
            {
                return singer;
            }

            public void setSinger(String singer) {
                this.singer = singer;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }

            public String getPic() {
                return pic;
            }

            public void setPic(String pic) {
                this.pic = pic;
            }

            public String getAlbum() {
                return album;
            }

            public void setAlbum(String album) {
                this.album = album;
            }
        }
    }
}

