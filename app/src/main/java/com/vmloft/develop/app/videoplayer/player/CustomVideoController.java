package com.vmloft.develop.app.videoplayer.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.OnClick;
import com.pili.pldroid.player.IMediaController;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.common.VApp;
import com.vmloft.develop.app.videoplayer.widget.PlayProgressBar;
import com.vmloft.develop.app.videoplayer.widget.PlaySeekBar;
import com.vmloft.develop.library.tools.VMApp;
import com.vmloft.develop.library.tools.utils.VMLog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Create by lzan13 on 2018/8/26
 * 自定义视频播放控制器
 */
public class CustomVideoController extends FrameLayout implements IMediaController {

    private static final int CTRL_HIDE = 1;
    private static final int CTRL_SHOW = 2;
    private static final int SEEK_BAR_DELAY = 200;

    private Context mContext;

    // 视频播放控制接口
    private MediaPlayerControl mPlayerControl;

    // 音频管理类
    private AudioManager mAudioManager;
    private Runnable mSeekBarRunnable;

    // UI 控件
    private View mAnchorView;
    @BindView(R.id.layout_controller_container) View mRootView;
    @BindView(R.id.img_back) ImageView mBackView;
    @BindView(R.id.text_title) TextView mTitleView;
    @BindView(R.id.img_lock) ImageView mLockView;
    @BindView(R.id.img_play) ImageView mPlayView;
    @BindView(R.id.img_fullscreen) ImageView mFullscreenView;
    @BindView(R.id.text_play_time) TextView mPlayTimeView;
    @BindView(R.id.text_duration_time) TextView mDurationTimeView;
    @BindView(R.id.progress_bar_play) PlayProgressBar mProgressBar;
    @BindView(R.id.seek_bar_play) PlaySeekBar mSeekBar;

    // 控制界面是否显示
    private boolean isShowing = false;
    private boolean isLock = false;
    private boolean isFullscreen = false;
    private boolean isDragging = false;
    private boolean isInstantSeeking = true;

    // 视频控制界面显示超时时间
    private int mDefaultTimeout = 5000;
    // 设置总的持续时间
    private long mDuration;

    public CustomVideoController(Context context) {
        this(context, null);
    }

    public CustomVideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context.getApplicationContext();
        initControllerUI();
    }

    /**
     * 初始化控制器 UI
     */
    private void initControllerUI() {
        // 获取控制器 UI 布局
        LayoutInflater.from(mContext).inflate(R.layout.widget_video_controller, this);
        ButterKnife.bind(this);
    }

    /**
     * 设置控制界面显示当前播放视频标题
     */
    public void setTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * 控制界面点击事件
     */
    @OnClick({ R.id.img_back, R.id.img_lock, R.id.img_play, R.id.img_fullscreen })
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.img_back:
            onBack();
            break;
        case R.id.img_lock:
            onLock();
            break;
        case R.id.img_play:
            onPlay();
            break;
        case R.id.img_fullscreen:
            onFullscreen();
            break;
        }
    }

    /**
     * 处理返回按钮事件
     */
    private void onBack() {
        if (isLock) {
            return;
        }
        if (isFullscreen) {
            exitFullscreen();
        }
        VApp.scanForActivity(mContext).onBackPressed();
    }

    /**
     * 处理锁定控制界面事件
     */
    private void onLock() {
        if (isLock) {
            isLock = false;
            isShowing = false;
            mLockView.setImageResource(R.drawable.ic_lock_open);
            show();
        } else {
            isLock = true;
            mLockView.setImageResource(R.drawable.ic_lock_close);
            mRootView.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    /**
     * 处理播放事件
     */
    private void onPlay() {
        if (mPlayerControl.isPlaying()) {
            mPlayerControl.pause();
        } else {
            mPlayerControl.start();
        }
        updatePlayStatus();
    }

    /**
     * 处理全屏事件
     */
    private void onFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     * 切换横屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期
     */
    public void enterFullscreen() {
        if (isFullscreen) {
            return;
        }

        VApp.scanForActivity(mContext)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ViewGroup contentView = VApp.scanForActivity(mContext).findViewById(android.R.id.content);
        isFullscreen = true;
    }

    /**
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     * 切换竖屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期.
     *
     * @return true退出全屏.
     */
    public boolean exitFullscreen() {
        if (isFullscreen) {
            VApp.scanForActivity(mContext)
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            ViewGroup contentView = VApp.scanForActivity(mContext)
                .findViewById(android.R.id.content);
            isFullscreen = false;
            return true;
        }
        return false;
    }

    /**
     * 更新播放暂停状态
     */
    private void updatePlayStatus() {
        if (mPlayerControl.isPlaying()) {
            mPlayView.setImageResource(R.drawable.ic_pause_circle_filled);
        } else {
            mPlayView.setImageResource(R.drawable.ic_play_circle_filled);
        }
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayerControl) {
        mPlayerControl = mediaPlayerControl;
    }

    @Override
    public void show() {
        show(mDefaultTimeout);
    }

    @Override
    public void show(int timeout) {
        if (isShowing) {
            return;
        }
        // 锁定状态一直显示底部的进度
        mLockView.setVisibility(VISIBLE);
        if (isLock) {
            mRootView.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
        } else {
            mRootView.setVisibility(VISIBLE);
            mProgressBar.setVisibility(GONE);
        }
        isShowing = true;
        if (timeout != 0) {
            mHandler.removeMessages(CTRL_HIDE);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(CTRL_HIDE), timeout);
        }
    }

    @Override
    public void hide() {
        if (isShowing) {
            mRootView.setVisibility(GONE);
            mLockView.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
            mHandler.removeMessages(CTRL_SHOW);
            isShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 是否拦截返回
     */
    public boolean interceptBack() {
        return isLock || isFullscreen;
    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public void setAnchorView(View view) {
        mAnchorView = view;
    }

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
            case CTRL_HIDE:
                hide();
                break;
            case CTRL_SHOW:
                show();
                break;
            }
        }
    };
}
