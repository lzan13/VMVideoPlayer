package com.vmloft.develop.app.videoplayer.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.OnClick;

import com.pili.pldroid.player.IMediaController;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.common.VApp;
import com.vmloft.develop.app.videoplayer.widget.PlayProgressBar;
import com.vmloft.develop.app.videoplayer.widget.PlaySeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.vmloft.develop.library.tools.utils.VMLog;
import java.util.Formatter;
import java.util.Locale;

/**
 * Create by lzan13 on 2018/8/26
 * 自定义视频播放控制器
 */
public class CustomController extends FrameLayout implements IMediaController {

    private static final int CTRL_HIDE = 1;
    private static final int CTRL_SHOW = 2;
    private static final int CTRL_PROGRESS = 3;
    private static final int SEEK_BAR_DELAY = 200;

    public static final int ACTION_BACK = 0x01;
    public static final int ACTION_LOCK = 0x02;
    public static final int ACTION_PLAY = 0x03;
    public static final int ACTION_FULLSCREEN = 0x04;

    private Context mContext;
    // 控制动作监听接口
    private OnCtrlActionListener mActionListener;

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

    public CustomController(Context context) {
        this(context, null);
    }

    public CustomController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化控制器
     */
    private void init(Context context) {
        mContext = context.getApplicationContext();
        // 获取控制器 UI 布局
        LayoutInflater.from(mContext).inflate(R.layout.widget_video_controller, this);
        ButterKnife.bind(this);

        setSeekBarUpdateListener();
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
            onBack(true);
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
    private boolean onBack(boolean callbackUI) {
        if (isLock) {
            return true;
        }
        if (isFullscreen) {
            onFullscreen();
            return true;
        }
        if (callbackUI) {
            VApp.scanForActivity(mContext).onBackPressed();
        }
        return false;
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
     * 旋转 UI
     */
    public void onFullscreen() {
        if (mActionListener != null) {
            mActionListener.onAction(ACTION_FULLSCREEN);
        }
        if (isFullscreen) {
            isFullscreen = false;
        } else {
            isFullscreen = true;
        }
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

    private void setSeekBarUpdateListener() {
        mSeekBar.setOnSeekUpdateListener(new PlaySeekBar.OnSeekUpdateListener() {
            @Override
            public void onSeekRelease() {
                long position = mPlayerControl.getDuration() * mSeekBar.getProgress() / mSeekBar.getMax();
                mPlayerControl.seekTo(position);
                VMLog.i("onSeekRelease %d", position);
            }

            @Override
            public void onSeekUpdate(int position, boolean fromUser) {
                VMLog.i("onSeekUpdate %d, %b", position, fromUser);
                if (fromUser && mPlayerControl != null) {
                    long progress = mPlayerControl.getDuration() * position / mSeekBar.getMax();
                    mPlayerControl.seekTo(progress);
                }
                mProgressBar.setProgress(position);
                mSeekBar.setProgress(position);
            }
        });
    }

    /**
     * 更新播放进度
     */
    private void updateProgress() {
        setProgress();
        if (mPlayerControl.isPlaying()) {
            mHandler.sendEmptyMessageDelayed(CTRL_PROGRESS, 1000);
        }
    }

    /**
     * 设置播放控制进度
     */
    private long setProgress() {
        if (mPlayerControl == null || isDragging) {
            return 0;
        }
        long position = mPlayerControl.getCurrentPosition();
        long duration = mPlayerControl.getDuration();
        int progress = (int) (mSeekBar.getMax() * position / duration);
        if (duration > 0) {
            mProgressBar.setProgress(progress);
            mSeekBar.setProgress(progress);
        }
        int bufferPercentage = mPlayerControl.getBufferPercentage();
        progress = (int) (mSeekBar.getMax() * bufferPercentage / 100f);
        mProgressBar.setSecondaryProgress(progress);
        mSeekBar.setSecondaryProgress(progress);

        mDuration = duration;
        if (mDurationTimeView != null) {
            mDurationTimeView.setText(generateTime(mDuration));
        }
        if (mPlayTimeView != null) {
            mPlayTimeView.setText(generateTime(position));
        }
        return position;
    }

    /**
     * 计算时间
     */
    private static String generateTime(long milliseconds) {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayerControl) {
        mPlayerControl = mediaPlayerControl;
    }

    @Override
    public void show() {
        show(mDefaultTimeout);
        updateProgress();
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

    public boolean isLock() {
        return isLock;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    /**
     * 是否拦截返回
     */
    public boolean interceptBack() {
        return onBack(false);
    }

    @Override
    public void setEnabled(boolean b) {}

    @Override
    public void setAnchorView(View view) {
        mAnchorView = view;
    }

    /**
     * 设置控制界面动作监听接口
     */
    public void setOnCtrlActionListener(OnCtrlActionListener listener) {
        mActionListener = listener;
    }

    /**
     * 定义控制界面控制动监听接口
     */
    public interface OnCtrlActionListener {

        void onAction(int action);
    }

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CTRL_HIDE:
                hide();
                break;
            case CTRL_SHOW:
                show();
                break;
            case CTRL_PROGRESS:
                mHandler.removeMessages(CTRL_PROGRESS);
                updateProgress();
                break;
            }
        }
    };
}
