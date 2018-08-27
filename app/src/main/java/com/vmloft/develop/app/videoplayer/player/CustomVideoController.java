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
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pili.pldroid.player.IMediaController;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.utils.VMLog;

/**
 * Create by lzan13 on 2018/8/26
 * 自定义视频播放控制器
 */
public class CustomVideoController extends FrameLayout implements IMediaController {
    private static final int CTRL_HIDE = 1;
    private static final int CTRL_SHOW = 2;
    private static final int SEEK_BAR_DELAY = 200;

    private Context mContext;

    private MediaPlayerControl mPlayerControl;

    // 控制器界面，这里用 PopupWindow 加载显示
    private PopupWindow mControllerWindow;
    private int mAnimStyle;

    // 音频管理类
    private AudioManager mAudioManager;
    // 视频播放控制接口
    private MediaPlayerControl mControl;
    private Runnable mSeekBarRunnable;

    // UI 控件
    private View mRootView;
    private View mAnchorView;
    private ImageView mLockView;
    private ImageView mPlayView;
    private ImageView mFullscreenView;
    private TextView mPlayTimeView;
    private TextView mDurationTimeView;
    //    private PlayProgressBar mProgressBar;
    private SeekBar mSeekBar;

    // 控制界面是否显示
    private boolean isShowing = false;
    private boolean isDragging = false;
    private boolean isInstantSeeking = true;

    // 视频控制界面显示超时时间
    private int mDefaultTimeout = 3000;
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
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.widget_video_controller, this);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayerControl) {
        mControl = mediaPlayerControl;
    }

    @Override
    public void show() {
        show(mDefaultTimeout);
    }

    @Override
    public void show(int timeout) {
        VMLog.i("开始显示控制界面");
        if (isShowing) {
            VMLog.e("控制界面已经显示");
            return;
        }
        mRootView.setVisibility(VISIBLE);
        isShowing = true;
        if (timeout != 0) {
            VMLog.i("发送隐藏控制界面的 handler 消息");
            mHandler.removeMessages(CTRL_HIDE);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(CTRL_HIDE), timeout);
        }
    }

    @Override
    public void hide() {
        VMLog.i("开始隐藏控制界面");
        if (isShowing && mRootView.isShown()) {
            VMLog.i("控制界面正在显示");
            mHandler.removeMessages(CTRL_SHOW);
            mRootView.setVisibility(GONE);
            isShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public void setAnchorView(View view) {
        mAnchorView = view;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
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
