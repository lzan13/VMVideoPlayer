package com.vmloft.develop.app.videoplayer.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.pili.pldroid.player.IMediaController;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.widget.PlayProgressBar;
import java.util.Locale;

/**
 * Create by lzan13 on 2018/8/26
 * 自定义视频播放控制器
 */
public class CustomVideoController extends FrameLayout implements IMediaController {

    private static final int CTRL_HIDE = 1;
    private static final int CTRL_SHOW = 2;
    private static final int SEEK_BAR_DELAY = 200;

    private Context mContext;

    // 音频管理类
    private AudioManager mAudioManager;
    // 视频播放控制接口
    private MediaPlayerControl mPlayerControl;
    private Runnable mSeekBarRunnable;

    // UI 控件
    private View mRootView;
    private View mAnchorView;
    private ImageView mLockView;
    private ImageView mPlayView;
    private ImageView mFullscreenView;
    private TextView mPlayTimeView;
    private TextView mDurationTimeView;
    private PlayProgressBar mProgressBar;
    private SeekBar mSeekBar;

    // 控制界面是否显示
    private boolean isShowing = false;
    private boolean isDragging = false;
    private boolean isInstantSeeking = true;

    // 视频控制界面显示超时时间
    private int mDefaultTimeout = 3000;
    // 设置总的持续时间
    private long mDuration;

    public CustomVideoController(@NonNull Context context) {
        this(context, null);
    }

    public CustomVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context.getApplicationContext();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.widget_video_controller, this);

        mRootView = this;

        mLockView = findViewById(R.id.img_lock);
        mPlayView = findViewById(R.id.img_play);
        mFullscreenView = findViewById(R.id.img_fullscreen);
        mPlayTimeView = findViewById(R.id.text_play_time);
        mDurationTimeView = findViewById(R.id.text_duration_time);
        // mProgressBar = findViewById(R.id.);
        mSeekBar = findViewById(R.id.seek_bar_play);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(mDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(mDefaultTimeout);
        return false;
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
        if (!isShowing) {
            setVisibility(View.VISIBLE);
            isShowing = true;
        }
        updatePlayStatus();
        mHandler.sendEmptyMessage(CTRL_SHOW);

        if (timeout != 0) {
            mHandler.removeMessages(CTRL_HIDE);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(CTRL_HIDE), timeout);
        }
    }

    @Override
    public void hide() {
        if (isShowing) {
            setVisibility(View.GONE);
            mHandler.removeMessages(CTRL_SHOW);
            isShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setAnchorView(View view) {

    }

    private void updatePlayStatus() {

    }

    /**
     * 设置播放进度
     */
    private long setProgress() {
        if (mPlayerControl == null || isDragging) {
            return 0;
        }

        long position = mPlayerControl.getCurrentPosition();
        long duration = mPlayerControl.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mPlayerControl.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

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
     * 根据播放进度计算时间
     *
     * @param position 当前播放位置
     */
    private static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 拖动条监听
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
            isDragging = true;
            show(3600000);
            mHandler.removeMessages(CTRL_SHOW);
            if (isInstantSeeking) {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            final long newPosition = (mDuration * progress) / 1000;
            String time = generateTime(newPosition);
            if (isInstantSeeking) {
                mHandler.removeCallbacks(mSeekBarRunnable);
                mSeekBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mPlayerControl.seekTo(newPosition);
                    }
                };
                mHandler.postDelayed(mSeekBarRunnable, SEEK_BAR_DELAY);
            }
            if (mPlayTimeView != null) {
                mPlayTimeView.setText(time);
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!isInstantSeeking) {
                mPlayerControl.seekTo(mDuration * bar.getProgress() / 1000);
            }

            show(mDefaultTimeout);
            mHandler.removeMessages(CTRL_SHOW);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            mHandler.sendEmptyMessageDelayed(CTRL_SHOW, 1000);
        }
    };

    /**
     * 处理控制界面的显示和隐藏
     */
    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
            case CTRL_HIDE:
                hide();
                break;
            case CTRL_SHOW:
                if (!mPlayerControl.isPlaying()) {
                    return;
                }
                pos = setProgress();
                if (pos == -1) {
                    return;
                }
                if (!isDragging && isShowing) {
                    msg = obtainMessage(CTRL_SHOW);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                    updatePlayStatus();
                }
                break;
            }
        }
    };
}
