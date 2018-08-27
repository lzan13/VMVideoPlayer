package com.vmloft.develop.app.videoplayer.network;

import com.vmloft.develop.app.videoplayer.bean.VideoBean;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Create by lzan13 on 18/8/27 下午6:12
 */
public interface INetAPI {
    @GET("/")
    public Call<VideoBean> getVideoList();
}
