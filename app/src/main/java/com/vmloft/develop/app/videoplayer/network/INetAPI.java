package com.vmloft.develop.app.videoplayer.network;

import com.vmloft.develop.app.videoplayer.bean.ResponseBean;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.bean.VideoSimpleBean;
import com.vmloft.develop.app.videoplayer.bean.VideoRankListBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Create by lzan13 on 18/8/27 下午6:12
 */
public interface INetAPI {

    /**
     * 表单提交要加 @FormUrlEncoded
     *
     * @param channel_id（频道ID）
     * @param tag_id：（标签ID）
     * @param keyword：（关键字（标题/简介模糊搜索））
     * @param orderby：（排序依据：0.默认排序     1.按播放量排序 2.按发布时间排序）
     * @param ordertype：（排序类型：0.降序     1.升序）
     * @param p：（分页，第几页）
     * @param r：（分页，每页显示多少条数据）
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?s=/Video/index")
    Call<ResponseBean<List<VideoSimpleBean>>>
    getVideoList(
            @Field("channel_id") String channel_id,
            @Field("tag_id") String tag_id,
            @Field("keyword") String keyword,
            @Field("orderby") String orderby,
            @Field("ordertype") String ordertype,
            @Field("p") String p,
            @Field("r") String r);

    /**
     * 关联视频列表
     *
     * @param id（视频ID）
     * @param p：（分页，第几页）
     * @param r：（分页，每页显示多少条数据）
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?s=/Video/same")
    Call<ResponseBean<List<VideoSimpleBean>>> getVideoListRelated(
            @Field("id") String id,
            @Field("p") String p,
            @Field("r") String r);

    /**
     * 排行榜
     *
     * @param type             排行榜类型：0.总榜 1.周榜 2.月榜
     * @param p：（分页，第几页）
     * @param r：（分页，每页显示多少条数据）
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?s=/Video/ranking")
    Call<ResponseBean<List<VideoRankListBean>>> getRankVideoList(
            @Field("type") int type,
            @Field("p") String p,
            @Field("r") String r);

    /**
     * 视频详情
     *
     * @param id
     * @param token
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?s=/Video/detail")
    Call<ResponseBean<VideoDetailBean>> getVideoDetail(
            @Field("id") String id,
            @Field("token") String token
    );
}
