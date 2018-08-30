package com.vmloft.develop.app.videoplayer.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.OnClick;

import com.pili.pldroid.player.IMediaController;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.common.VApp;
import com.vmloft.develop.app.videoplayer.common.VBrightness;
import com.vmloft.develop.app.videoplayer.widget.CustomProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMLog;

import java.util.Formatter;
import java.util.Locale;

import static android.content.ContentValues.TAG;

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
    // 滑动阀值
    private static final int THRESHOLD = 20;
    public static final int MAX_SEEK_DURATION = 90 * 1000;

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
    @BindView(R.id.progress_bar_play) CustomProgressBar mProgressBar;
    @BindView(R.id.seek_bar_play) SeekBar mSeekBar;
    @BindView(R.id.layout_ctrl_volume_brightness) RelativeLayout mCtrlVolumeBrightnessLayout;
    @BindView(R.id.img_ctrl_volume_brightness) ImageView mCtrlVolumeBrightnessView;
    @BindView(R.id.progress_ctrl_volume_brightness) ProgressBar mCtrlVolumeBrightnessProgressBar;
    @BindView(R.id.layout_seek_tip) RelativeLayout mSeekTipLayout;
    @BindView(R.id.text_seek_tip) TextView mSeekTipView;

    private VMActivity mActivity;
    private Context mContext;
    // 控制动作监听接口
    private OnCtrlActionListener mActionListener;

    // 视频播放控制接口
    private MediaPlayerControl mPlayerControl;

    // 音频管理类
    private AudioManager mAudioManager;

    private int mMaxVolume = 0;
    private int mCurrVolume = -1;
    private int mCurrBrightness = -1;
    private long mCurrPosition = 0l;
    private long mNewPosition = 0l;

    private boolean isNeedChangePosition = false;
    private boolean isNeedChangeVolume = false;
    private boolean isNeedChangeBrightness = false;
    private float mDownX;
    private float mDownY;

    // 控制界面是否显示
    private boolean isShowing = false;
    private boolean isLock = false;
    private boolean isFullscreen = false;
    private boolean isDragging = false;

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
     * 设置当前控制界面所依附的 activity
     */
    public void setActivity(VMActivity activity) {
        mActivity = activity;
    }

    /**
     * 初始化控制器
     */
    private void init(Context context) {
        mContext = context.getApplicationContext();
        mActivity = VApp.scanForActivity(mContext);
        // 获取控制器 UI 布局
        LayoutInflater.from(mContext).inflate(R.layout.widget_video_controller, this);
        ButterKnife.bind(this);

        mSeekBar.setMax(10000);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        setSeekBarUpdateListener();
        setViewTouchListener();
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
        updatePlayStatus();
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
        if (isShowing && !isDragging) {
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
     * 设置拖动条更新监听器
     */
    private void setSeekBarUpdateListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 主要是用于监听进度值的改变
                int position = progress;
                int playProgress = 0;
                if (mPlayerControl != null) {
                    playProgress = (int) (mPlayerControl.getDuration() * position / mSeekBar.getMax());
                }
                if (fromUser) {
                    if (mPlayTimeView != null) {
                        mPlayTimeView.setText(generateTime(playProgress));
                    }
                }
            }

            /**
             * 开始拖动进度条
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 监听用户开始拖动进度条的时候
                isDragging = true;
                int position = mSeekBar.getProgress();
                int playProgress = 0;
                if (mPlayerControl != null) {
                    playProgress = (int) (mPlayerControl.getDuration() * position / mSeekBar.getMax());
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 监听用户结束拖动进度条的时候
                int position = seekBar.getProgress();
                int playProgress = 0;
                if (mPlayerControl != null) {
                    playProgress = (int) (mPlayerControl.getDuration() * position / mSeekBar.getMax());
                    mPlayerControl.seekTo(playProgress);
                }
                mProgressBar.setProgress(position);
                mSeekBar.setProgress(position);
                if (mPlayTimeView != null) {
                    mPlayTimeView.setText(generateTime(playProgress));
                }
                isDragging = false;
            }
        });
    }

    /**
     * 设置 View 触摸事件监听器
     */
    private void setViewTouchListener() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                if (isLock) {
                    hideCtrlVolumeBrightness();
                    return false;
                }
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = x;
                    mDownY = y;
                    isNeedChangePosition = false;
                    isNeedChangeVolume = false;
                    isNeedChangeBrightness = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (!isNeedChangePosition && !isNeedChangeVolume && !isNeedChangeBrightness) {
                        if (absDeltaX >= THRESHOLD) {
                            isDragging = true;
                            isNeedChangePosition = true;
                            mCurrPosition = mPlayerControl.getCurrentPosition();
                        } else if (absDeltaY >= THRESHOLD) {
                            if (mDownX < getWidth() * 0.5f) {
                                // 左侧改变亮度
                                isNeedChangeBrightness = true;
                                if (mActivity != null) {
                                    float bright = -1;
                                    try {
                                        WindowManager.LayoutParams lp = mActivity.getWindow()
                                            .getAttributes();
                                        bright = lp.screenBrightness;
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    if (bright > 0) {
                                        mCurrBrightness = Math.min(255, (int) (255 * bright));
                                    } else {
                                        mCurrBrightness = VBrightness.getScreenBrightness(mContext);
                                    }
                                }
                            } else {
                                // 右侧改变声音
                                isNeedChangeVolume = true;
                                mCurrVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            }
                        }
                    }
                    if (isNeedChangePosition) {
                        long duration = mPlayerControl.getDuration();
                        long toPosition = (long) (mCurrPosition + MAX_SEEK_DURATION * deltaX * 1.0f / getWidth());
                        mNewPosition = Math.max(0, Math.min(duration, toPosition));
                        updatePosition(mNewPosition);
                    }
                    if (isNeedChangeBrightness) {
                        deltaY = -deltaY;
                        int deltaBrightness = (int) (deltaY * 255.0 / getHeight());
                        updateBrightness(deltaBrightness);
                    }
                    if (isNeedChangeVolume) {
                        deltaY = -deltaY;
                        float deltaVolume = (mMaxVolume * deltaY * 3 / getHeight());
                        updateVolume(deltaVolume);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isDragging = false;
                    if (isNeedChangePosition) {
                        mPlayerControl.seekTo(mNewPosition);
                        hideSeekTipLayout();
                        return true;
                    }
                    if (isNeedChangeBrightness) {
                        hideCtrlVolumeBrightness();
                        return true;
                    }
                    if (isNeedChangeVolume) {
                        hideCtrlVolumeBrightness();
                        return true;
                    }
                    if (isShowing) {
                        hide();
                    } else {
                        show();
                    }
                    break;
                }
                return true;
            }
        });
    }

    /**
     * 滑动快进快退
     */
    private void updatePosition(long position) {
        mSeekTipLayout.setVisibility(VISIBLE);
        String pos = generateTime(position);
        String dur = generateTime(mDuration);
        mSeekTipView.setText(pos + "/" + dur);
    }

    private void hideSeekTipLayout() {
        mSeekTipLayout.setVisibility(INVISIBLE);
    }

    /**
     * 滑动改变声音大小
     */
    private void updateVolume(float deltaVolume) {
        VMLog.i("CustomController updateVolume %f", deltaVolume);
        mCtrlVolumeBrightnessLayout.setVisibility(VISIBLE);
        mCtrlVolumeBrightnessView.setImageResource(R.drawable.ic_volume);

        float newVolume = mCurrVolume + deltaVolume;
        newVolume = Math.max(0, Math.min(mMaxVolume, newVolume));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) newVolume, 0);
        int newVolumeProgress = (int) (100 * newVolume / mMaxVolume);
        mCtrlVolumeBrightnessProgressBar.setProgress(newVolumeProgress);
    }

    /**
     * 滑动改变亮度
     */
    private void updateBrightness(int deltaBrightness) {
        VMLog.i("CustomController updateBrightness %d", deltaBrightness);
        mCtrlVolumeBrightnessLayout.setVisibility(VISIBLE);
        mCtrlVolumeBrightnessView.setImageResource(R.drawable.ic_brightness);

        int newBrightness = mCurrBrightness + deltaBrightness;
        newBrightness = Math.max(0, Math.min(newBrightness, 255));
        if (mActivity != null) {
            VBrightness.setBrightness(mActivity, newBrightness);
            float newBrightnessPercentage = newBrightness / 255.0f;
            int newBrightnessProgress = (int) (100f * newBrightnessPercentage);
            mCtrlVolumeBrightnessProgressBar.setProgress(newBrightnessProgress);
        }
    }

    private void hideCtrlVolumeBrightness() {
        mCtrlVolumeBrightnessLayout.setVisibility(INVISIBLE);
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
