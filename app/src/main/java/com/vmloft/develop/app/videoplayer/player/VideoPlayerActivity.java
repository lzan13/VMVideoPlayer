package com.vmloft.develop.app.videoplayer.player;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.common.VCallback;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.network.NetHelper;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMLog;

/**
 * Create by lzan13 on 2018/8/24
 */
public class VideoPlayerActivity extends VMActivity {

    private View playerContainer;
    private VideoPlayerFragment videoPlayerFragment;

    private String videoId;
    private VideoDetailBean videoDetailBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        init();
    }

    private void init() {
        playerContainer = findViewById(R.id.fragment_video_container);

        videoId = getIntent().getStringExtra(VConstant.KEY_VIDEO_ID);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
            .getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int playerHeight = screenWidth * 720 / 1280;
        playerContainer.getLayoutParams().height = playerHeight;

        requestVideoData();
    }

    private void requestVideoData() {
        NetHelper.getInstance().requestVideoDetail(videoId, new VCallback() {
            @Override
            public void onDone(Object object) {
                videoDetailBean = (VideoDetailBean) object;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initPlayerFragment();
                    }
                });
                VMLog.i("请求成功 %s", videoDetailBean.toString());
            }

            @Override
            public void onError(int code, String desc) {
                VMLog.e("请求失败: %d, %s", code, desc);
            }
        });
    }

    private void initPlayerFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (videoPlayerFragment != null) {
            videoPlayerFragment = null;
        }
        videoPlayerFragment = VideoPlayerFragment.newInstance(videoDetailBean);
        transaction.replace(R.id.fragment_video_container, videoPlayerFragment)
            .commitAllowingStateLoss();
    }

    //设置
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //int type = newConfig.orientation;
        //if (type == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        //    //横屏
        //    setContentView(R.layout.activity_swcamera_streaming);
        //} else if (type == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        //    //竖屏
        //    setContentView(R.layout.activity_swcamera_streaming);
        //}
        //Log.i("jinwei", "newConfig" + newConfig);
    }
    //重新setContentView这种方法可以达到横屏切换不重启，
    //布局可以刷新，但是会重新设置view，其他数据不会被销毁。

    @Override
    public void onBackPressed() {
        if (videoPlayerFragment.mVideoController.interceptBack()) {
            return;
        }
        super.onBackPressed();
    }
}
