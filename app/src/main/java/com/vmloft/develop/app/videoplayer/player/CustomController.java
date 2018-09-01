package com.vmloft.develop.app.videoplayer.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.Toast;
import butterknife.OnClick;

import com.pili.pldroid.player.IMediaController;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnImageCapturedListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoView;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.common.VApp;
import com.vmloft.develop.app.videoplayer.common.VBrightness;
import com.vmloft.develop.app.videoplayer.widget.CustomProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMFile;
import com.vmloft.develop.library.tools.utils.VMLog;

import com.vmloft.develop.library.tools.utils.bitmap.VMBitmap;
import java.io.IOException;
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
    @BindView(R.id.img_screenshot) ImageView mScreenshotView;
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

    // 视频播放控制接口
    private MediaPlayerControl mPlayerControl;
    private PLVideoView mVideoPlayView;

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
     * 设置当前控制界面所依附的 activity
     */
    public void setActivity(VMActivity activity) {
        mActivity = activity;
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
     * 初始化视频播放的一些监听，因为要设置给播放控件，所以这里传进来
     */
    public void initControllerListener(PLVideoView videoView) {
        mVideoPlayView = videoView;
        mVideoPlayView.setOnPreparedListener(mOnPreparedListener);
        mVideoPlayView.setOnInfoListener(mOnInfoListener);
        mVideoPlayView.setOnErrorListener(mOnErrorListener);
        mVideoPlayView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoPlayView.setOnCompletionListener(mOnCompletionListener);
        mVideoPlayView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        // 设置视频数据回调接口
        mVideoPlayView.setOnVideoFrameListener(mVideoFrameListener);
        // 设置截图回调接口
        mVideoPlayView.setOnImageCapturedListener(mImageCapturedListener);
    }

    /**
     * 控制界面点击事件
     */
    @OnClick({
        R.id.img_back, R.id.img_lock, R.id.img_play, R.id.img_screenshot, R.id.img_fullscreen
    })
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
        case R.id.img_screenshot:
            onScreenshot();
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

    private void onScreenshot() {
        mVideoPlayView.captureImage(3000);
    }

    /**
     * 处理全屏事件
     */
    @SuppressLint("RestrictedApi")
    private void onFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            ViewGroup contentView = mActivity.findViewById(android.R.id.content);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            isFullscreen = true;
            // 隐藏ActionBar、状态栏，并横屏
            ActionBar ab = mActivity.getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.hide();
            }
            mActivity.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            ViewGroup contentView = mActivity.findViewById(android.R.id.content);
            //this.removeView(mContainer);
            //LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //contentView.addView(mContainer, params);
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

    /**
     * ---------------------------------------------------------------------------------
     * 定义 PLDroidPLayer 的一些监听
     */

    private PLOnPreparedListener mOnPreparedListener = new PLOnPreparedListener() {
        @Override
        public void onPrepared(int prepared) {
            VMLog.i("onPrepared %d", prepared);
        }
    };
    /**
     * 播放信息监听
     */
    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            //VMLog.i("onInfo,what: %d, extra: %d", what, extra);
            switch (what) {
            case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                //                    mLoadView.setText("正在准备...");
                break;
            case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                break;
            case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                //VMLog.i("video frame rendering, ts = " + extra);
                break;
            case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                //VMLog.i("audio frame rendering, ts = " + extra);
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                //VMLog.i("Gop Time: " + extra);
                break;
            case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                //VMLog.i("Hardware decoding failure, switching software decoding!");
                break;
            case PLOnInfoListener.MEDIA_INFO_METADATA:
                //VMLog.i(mVideoPlayView.getMetadata().toString());
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
            case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                //updateStatInfo();
                break;
            case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                //VMLog.i("Connected !");
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                //VMLog.i("Rotation changed: " + extra);
            default:
                break;
            }
        }
    };

    /**
     * 错误信息监听
     */
    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            VMLog.e("onError happened, errorCode %d", errorCode);
            switch (errorCode) {
            case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                /**
                 * SDK will do reconnecting automatically
                 */
                VMLog.e("IO Error!");
                return false;
            case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                break;
            case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                break;
            default:
                break;
            }
            return true;
        }
    };

    /**
     * 播放完成监听
     */
    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            VMLog.i("Play Completed !");
        }
    };

    /**
     * 缓存更新监听
     */
    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            //VMLog.i("onBufferingUpdate: " + precent);
        }
    };

    /**
     * 视频大小变化监听
     */
    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            VMLog.i("onVideoSizeChanged: width = " + width + ", height = " + height);
        }
    };

    /**
     * 视频数据帧回调接口
     */
    private PLOnVideoFrameListener mVideoFrameListener = new PLOnVideoFrameListener() {

        /**
         * 回调一帧视频帧数据
         *
         * @param data   视频帧数据
         * @param size   数据大小
         * @param width  视频帧的宽
         * @param height 视频帧的高
         * @param format 视频帧的格式，0代表 YUV420P，1 代表 JPEG， 2 代表 SEI
         * @param ts     时间戳，单位是毫秒
         */
        @Override
        public void onVideoFrameAvailable(byte[] data, int size, int width, int height, int format, long ts) {
            VMLog.i("onVideoFrameAvailable size: %d, w: %d, h: %d, f: %d", size, width, height, format);
        }
    };

    /**
     * 视频截图回调
     */
    private PLOnImageCapturedListener mImageCapturedListener = new PLOnImageCapturedListener() {
        @Override
        public void onImageCaptured(byte[] bytes) {
            VMLog.i("onImageCaptured len: %d, %d, %d, %d", bytes.length, bytes[0], bytes[1], bytes[2]);
            String path = VMFile.getPictures() + "/Screenshots/" + "VMVideoPlayerScreenshot.png";
            VMFile.createDirectory(path);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            try {
                VMBitmap.saveBitmapToSDCard(bitmap, path);
                String toastStr = "截图已保存在路径：" + path;
                VMLog.i(toastStr);
                Toast.makeText(mContext, toastStr, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
