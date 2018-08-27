package com.vmloft.develop.app.videoplayer.network;

import com.vmloft.develop.app.videoplayer.bean.ResultBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Create by lzan13 on 18/8/27 下午6:12
 */
public interface INetAPI {

    @GET("api/news/feed/v51/")
    public Call<ResultBean> getVideoList(@Query("category") String category, @Query("refer") int refer);
}
