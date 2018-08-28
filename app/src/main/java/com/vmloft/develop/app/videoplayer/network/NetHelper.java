package com.vmloft.develop.app.videoplayer.network;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.ResponseBean;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.bean.VideoSimpleBean;
import com.vmloft.develop.app.videoplayer.common.VCallback;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.common.VError;
import com.vmloft.develop.library.tools.utils.VMLog;
import com.vmloft.develop.library.tools.utils.VMStr;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create by lzan13 on 18/8/27 下午6:12
 */
public class NetHelper {

    private static NetHelper instance;

    private OkHttpClient client;
    private Retrofit retrofit;
    private INetAPI netAPI;

    NetHelper() {
        // 实例化 OkHttpClient，如果不自己创建 Retrofit 也会创建一个默认的
        client = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                //                .addInterceptor(new RequestInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        // 实例化 Retrofit
        retrofit = new Retrofit.Builder().client(client)
                .baseUrl(VConstant.BASE_URL_1_1)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // 创建 Retrofit 接口实例
        netAPI = retrofit.create(INetAPI.class);
    }

    public static NetHelper getInstance() {
        if (instance == null) {
            instance = new NetHelper();
        }
        return instance;
    }

    /**
     * 为 POST 请求添加公共参数
     */
    private Request addPostFromParams(Request request) {
        FormBody.Builder builder = new FormBody.Builder();
        FormBody formBody = (FormBody) request.body();
        for (int i = 0; i < formBody.size(); i++) {
            builder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
        }
        builder.addEncoded("device", "lz_mi5").build();
        return request.newBuilder().post(builder.build()).build();
    }

    /**
     * 为 POST 请求添加公共参数，多参数情况（参数包含文件时）
     */
    private Request addPostMultipartParams(Request request) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("device", "lz_mi5");
        MultipartBody body = (MultipartBody) request.body();
        for (int i = 0; i < body.size(); i++) {
            builder.addPart(body.part(i));
        }
        return request.newBuilder().post(builder.build()).build();
    }

    /**
     * 为 GET 请求添加公共参数
     */
    private Request addGetParams(Request request) {
        HttpUrl.Builder builder = request.url().newBuilder();
        builder.addQueryParameter("device", "lz_mi5");
        return request.newBuilder().url(builder.build()).build();
    }

    /**
     * 自定义拦截器，用户添加公共参数操作
     */
    private class RequestInterceptor implements Interceptor {

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (request.method().equals("POST")) {
                // POST 请求有两种请求体形式 1.FormBody:表单形式，2.MultipartBody:多参数表单（包含文件）
                if (request.body() instanceof FormBody) {
                    addPostFromParams(request);
                } else if (request.body() instanceof MultipartBody) {
                    addPostMultipartParams(request);
                }
            } else if (request.method().equals("GET")) {
                addGetParams(request);
            }
            return chain.proceed(request);
        }
    }


    /**
     * 请求视频列表
     */
    public void requestVideoList(final VCallback callback) {
        Call<ResponseBean<List<VideoSimpleBean>>> call = netAPI.getVideoList(null, null, null, null, null, "0", "20");
        call.enqueue(new Callback<ResponseBean<List<VideoSimpleBean>>>() {
            @Override
            public void onResponse(Call<ResponseBean<List<VideoSimpleBean>>> call, Response<ResponseBean<List<VideoSimpleBean>>> response) {
                if (response.isSuccessful()) {
                    callback.onDone(response.body().getData());
                } else {
                    callback.onError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBean<List<VideoSimpleBean>>> call, Throwable t) {
                parseThrowable(t, callback);
            }
        });
    }

    /**
     * 请求视频详情
     */
    public void requestVideoDetail(String videoId, final VCallback callback) {
        Call<ResponseBean<VideoDetailBean>> call = netAPI.getVideoDetail(videoId, null);
        call.enqueue(new Callback<ResponseBean<VideoDetailBean>>() {
            @Override
            public void onResponse(Call<ResponseBean<VideoDetailBean>> call, Response<ResponseBean<VideoDetailBean>> response) {
                if (response.isSuccessful()) {
                    callback.onDone(response.body().getData());
                } else {
                    callback.onError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBean<VideoDetailBean>> call, Throwable t) {
                parseThrowable(t, callback);
            }
        });
    }

    /**
     * 请求出现异常错误处理
     *
     * @param e        异常
     * @param callback 回调
     */
    public void parseThrowable(Throwable e, VCallback callback) {
        int code;
        String msg = null;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            //httpException.response().errorBody().string()
            code = httpException.code();
            if (code == 500 || code == 404) {
                code = VError.SERVER;
                msg = VMStr.strByResId(R.string.err_server);
            }
        } else if (e instanceof ConnectException) {
            code = VError.NETWORK;
            msg = VMStr.strByResId(R.string.err_network);
        } else if (e instanceof SocketTimeoutException) {
            code = VError.TIMEOUT;
            msg = VMStr.strByResId(R.string.err_network);
        } else {
            code = VError.UNKNOWN;
            msg = VMStr.strByResId(R.string.err_unknown) + e.getMessage();
        }
        VMLog.e(msg);
        callback.onError(code, msg);
    }
}
