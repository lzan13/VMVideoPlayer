package com.vmloft.develop.app.videoplayer.bean;

/**
 * Created by chalilayang on 2017/11/25.
 */

public class ResponseBean<T> {
    private int status;
    private String msg;
    private String url;
    private T data;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String getUrl() {
        return url;
    }

    public T getData() {
        return data;
    }
}
