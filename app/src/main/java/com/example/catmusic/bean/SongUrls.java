package com.example.catmusic.bean;

import java.io.Serializable;
import java.util.Map;

public class SongUrls
{
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

    public static class ResultBean implements Serializable
    {
        private Map<String, String> map;

        public Map<String, String> getMap()
        {
            return map;
        }

        public void setMap(Map<String, String> map)
        {
            this.map = map;
        }
    }
}