package com.vmloft.develop.app.videoplayer.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.common.VCallback;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.network.NetHelper;
import com.vmloft.develop.app.videoplayer.widget.CustomProgressBar;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMDimen;
import com.vmloft.develop.library.tools.utils.VMLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create by lzan13 on 2018/8/24
 */
public class VideoPlayerActivity extends VMActivity {

    @BindView(R.id.fragment_video_container) FrameLayout playerContainer;

    private VideoPlayerFragment videoPlayerFragment;

    private String videoId;
    private VideoDetailBean videoDetailBean;

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        ButterKnife.bind(activity);

        init();
    }

    private void init() {
        videoId = getIntent().getStringExtra(VConstant.KEY_VIDEO_ID);
        changeLayoutParam();
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
                VMLog.i("请求视频详情成功 %s", videoDetailBean.toString());
            }

            @Override
            public void onError(int code, String desc) {
                VMLog.e("请求视频详情失败: %d, %s", code, desc);
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

    @Override
    public void onBackPressed() {
        if (videoPlayerFragment.mController.interceptBack()) {
            return;
        }
        super.onBackPressed();
    }

    private void changeLayoutParam() {
        checkScreenSize();
        float vRatio = (float) 720 / 1280;
        float sRatio = (float) mScreenHeight / mScreenWidth;
        int playWidth = 0;
        int playHeight = 0;
        if (vRatio > sRatio) {
            // 如果视频比例比屏幕比例大，则需要视频高度充满屏幕，宽度计算
            playWidth = (int) (mScreenHeight / vRatio);
            playHeight = mScreenHeight;
        } else {
            // 如果视频比例比屏幕比例小，则视频宽度充满屏幕，高度计算
            playWidth = mScreenWidth;
            playHeight = (int) (mScreenWidth * vRatio);
        }

        playerContainer.getLayoutParams().height = playHeight;
        if (videoPlayerFragment != null) {
            videoPlayerFragment.changeVideoViewSize(playWidth, playHeight);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            VMLog.i("屏幕方向变化，当前为竖屏模式");
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            VMLog.i("屏幕方向变化，当前为横屏模式");
        } else {
            VMLog.i("屏幕方向变化，不知道当前什么模式");
        }
        changeLayoutParam();
    }

    /**
     * 检查计算屏幕大小
     */
    private void checkScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }
}
