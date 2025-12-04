package com.example.catmusic.bean;

import java.io.Serializable;
import java.util.List;

public class Recommend
{

    /**
     * code : 0
     * result : {"sliders":[{"id":"67421058","pic":"https://y.qq.com/music/common/upload/MUSIC_FOCUS/7032830.png","link":"https://y.qq.com/n/yqq/album/003ryYZm47hg1r.html"},{"id":"https://y.qq.com/forest/fb9z6tPkopIVsP4G/index.html?ADTAG=hz_faneconomy_PCyyg&openinqqmusic=1&keep_cid=1","pic":"https://y.qq.com/music/common/upload/MUSIC_FOCUS/7032745.jpg","link":"https://y.qq.com/forest/fb9z6tPkopIVsP4G/index.html?ADTAG=hz_faneconomy_PCyyg&openinqqmusic=1&keep_cid=1"},{"id":"67445700","pic":"https://y.qq.com/music/common/upload/ocs/a784ddef0b4632a2c9fa822a16f5a6a6.jpg","link":"https://y.qq.com/n/yqq/album/000UiwBp1KkHi5.html"},{"id":"67428071","pic":"https://y.qq.com/music/common/upload/ocs/b50c9a58cd0d46b8a62d6fcec6399cb8.jpg","link":"https://y.qq.com/n/yqq/album/004NMEGS2vFrwM.html"},{"id":"66749907","pic":"https://y.qq.com/music/common/upload/ocs/804b7677cbb7a66e41e91546787d2645.jpg","link":"https://y.qq.com/n/yqq/album/000gRf352cKK9l.html"},{"id":"67315328","pic":"https://y.qq.com/music/common/upload/ocs/56b042e510ba61d37e109ee7f9f1f57b.jpg","link":"https://y.qq.com/n/yqq/album/002EOobn1LMuLV.html"},{"id":"67317229","pic":"https://y.qq.com/music/common/upload/MUSIC_FOCUS/7033097.jpg","link":"https://y.qq.com/n/yqq/album/002cGk8c06OfJt.html"}],"albums":[{"id":8075336924,"username":"听风","title":"90后回忆 | MP3时代的流行歌曲","pic":"http://qpic.y.qq.com/music_cover/I2ZdwiaF8XY3CVB1y18cmH6dVjiaC6hprhowF1emvMrTFIxCibB04GH5A/300?n=1"},{"id":7981001167,"username":"Koi","title":"甜系rap | 你与星河 皆可收藏","pic":"http://qpic.y.qq.com/music_cover/Y9XiclYnNeIBxAmia562dPQfroZbsLU9wB4FotjfTVlqzIvep8uZWUnQ/300?n=1"},{"id":7861817748,"username":"幻菱","title":"效率加倍丨学习工作专属轻音乐","pic":"http://qpic.y.qq.com/music_cover/dq5UenayM4jZR6dQv1kVkxanNr786ZVFzLxMgHKuTuFeo7LGokULOA/300?n=1"},{"id":7885396294,"username":"今天星期几","title":"甜蜜暴击：恋爱党单曲循环的调调","pic":"http://qpic.y.qq.com/music_cover/7axJgSibuQNcws5JAbn8TT6sia6Kp4RxzupgNqibE3uibvg2hFRfoF2XXQ/300?n=1"},{"id":7671841486,"username":"          澪兮Li、","title":"人间失格丨纯洁无瑕的信赖是种罪","pic":"http://qpic.y.qq.com/music_cover/ibBMZnEfJHDxMp5SvGibcHictuVPSusVEm3icfZIj9WyWvOEsplhVfgB4Q/300?n=1"},{"id":7194652332,"username":"Sky","title":"流行 | 释放压力时必备歌单","pic":"http://qpic.y.qq.com/music_cover/ib1G8O9KWTTHDeps27QvvzuKribpkkWRayWcKbibPNPqOuthUKbQ1Vy0Q/300?n=1"},{"id":7738136801,"username":"PINK-","title":"恋爱必听！甜到齁的rap情歌","pic":"http://qpic.y.qq.com/music_cover/4sLx94ibHnz1nDfLQHYM6IKpic6xG9CYpaT96icsoOekF0thDzE9Y2F3A/300?n=1"},{"id":7657295368,"username":"艾司唑仑","title":"【咚鼓热歌】柔情似水，痴情如火","pic":"http://qpic.y.qq.com/music_cover/bJKTW68rucuwkicG3kDH8hoe44vGAWbXTJtnA5P9mOPibSqyk5icDxOXQ/300?n=1"},{"id":7942692926,"username":"infinite\u2014hell","title":"古风｜以吾之剑，诛杀一切邪魔！","pic":"http://qpic.y.qq.com/music_cover/bg2CqbicKIKejpySXUicR4rSHA5uysPIoGH9jSALnZxd574MVZBaBhuA/300?n=1"},{"id":7690757028,"username":"沙丁鱼和奶油","title":"古风丨红尘一醉，愿得一人心","pic":"http://qpic.y.qq.com/music_cover/bFQsibroB8LXJhoIUKkjbJNLTCN5eMYYZJzOFtPT6VamuxqMA8zuuKs6pA73PUiapo/300?n=1"},{"id":7734895712,"username":"黑白灰蓝","title":"粤难粤爱 | 靠拥抱亦难任你拥有","pic":"http://qpic.y.qq.com/music_cover/I0IR30Er2zO52NicJBkFCUqQ88k02Za8bDXpibWSdHDtgzmMY7ibrTRmg/300?n=1"},{"id":7824947011,"username":"胖桃","title":"入耳即心动，收下这份御姐音","pic":"http://qpic.y.qq.com/music_cover/W6nHribvOiaYondR2Y3Bt0Hg6qNak2En2BRa5umDp2C63Czhc2IZzbug/300?n=1"}]}
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
        private List<SlidersBean> sliders;
        private List<AlbumsBean> albums;

        public List<SlidersBean> getSliders()
        {
            return sliders;
        }

        public void setSliders(List<SlidersBean> sliders)
        {
            this.sliders = sliders;
        }

        public List<AlbumsBean> getAlbums()
        {
            return albums;
        }

        public void setAlbums(List<AlbumsBean> albums)
        {
            this.albums = albums;
        }

        public static class SlidersBean
        {
            /**
             * id : 67421058
             * pic : https://y.qq.com/music/common/upload/MUSIC_FOCUS/7032830.png
             * link : https://y.qq.com/n/yqq/album/003ryYZm47hg1r.html
             */

            private String id;
            private String pic;
            private String link;

            public String getId()
            {
                return id;
            }

            public void setId(String id)
            {
                this.id = id;
            }

            public String getPic()
            {
                return pic;
            }

            public void setPic(String pic)
            {
                this.pic = pic;
            }

            public String getLink()
            {
                return link;
            }

            public void setLink(String link)
            {
                this.link = link;
            }
        }

        public static class AlbumsBean implements Serializable
        {
            /**
             * id : 8075336924
             * username : 听风
             * title : 90后回忆 | MP3时代的流行歌曲
             * pic : http://qpic.y.qq.com/music_cover/I2ZdwiaF8XY3CVB1y18cmH6dVjiaC6hprhowF1emvMrTFIxCibB04GH5A/300?n=1
             */

            private long id;
            private String username;
            private String title;
            private String pic;

            public long getId()
            {
                return id;
            }

            public void setId(long id)
            {
                this.id = id;
            }

            public String getUsername()
            {
                return username;
            }

            public void setUsername(String username)
            {
                this.username = username;
            }

            public String getTitle()
            {
                return title;
            }

            public void setTitle(String title)
            {
                this.title = title;
            }

            public String getPic()
            {
                return pic;
            }

            public void setPic(String pic)
            {
                this.pic = pic;
            }
        }
    }
}