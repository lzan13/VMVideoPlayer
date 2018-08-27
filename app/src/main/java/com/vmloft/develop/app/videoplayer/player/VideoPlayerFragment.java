package com.vmloft.develop.app.videoplayer.player;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.vmloft.develop.app.videoplayer.Constant;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.VMActivity;

public class VideoPlayerFragment extends Fragment {

    private static final String TAG = "VideoPlayerFragment";
    private VMActivity activity;

    @BindView(R.id.view_video_player)
    PLVideoTextureView mVideoPlayView;
    @BindView(R.id.layout_loading)
    LinearLayout mLoadingLayout;
    @BindView(R.id.img_cover)
    ImageView mCoverView;
    @BindView(R.id.text_status_info)
    TextView mStatusView;

    @BindView(R.id.custom_video_controller)
    CustomVideoController mVideoController;
    private String videoPath;
    private int mRotation = 0;

    private boolean mIsLiveStreaming = false;
    private int mDisplayAspectRatio = PLVideoTextureView.ASPECT_RATIO_ORIGIN;

    public static VideoPlayerFragment newInstance(String videoPath) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(Constant.KEY_VIDEO_DETAIL, videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoPath = getArguments().getString(Constant.KEY_VIDEO_DETAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (VMActivity) getActivity();
        init();
    }

    protected void init() {
        ButterKnife.bind(this, getView());

        mIsLiveStreaming = false;

        mVideoPlayView.setBufferingIndicator(mLoadingLayout);

        mVideoPlayView.setCoverView(mCoverView);

        // If you want to fix display orientation such as landscape, you can use the code show as follow
        //
        // if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //     mVideoPlayView.setPreviewOrientation(0);
        // }
        // else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        //     mVideoPlayView.setPreviewOrientation(270);
        // }

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = AVOptions.MEDIA_CODEC_SW_DECODE;
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, mIsLiveStreaming ? 1 : 0);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        boolean disableLog = false;
        options.setInteger(AVOptions.KEY_LOG_LEVEL, disableLog ? 5 : 0);
        // 设置拖动定位方式，1 为精准模式，即会拖动到时间戳的那一秒；0 为普通模式，会拖动到时间戳最近的关键帧。默认为 0
        options.setInteger(AVOptions.KEY_SEEK_MODE, 1);
        boolean cache = false;
        if (!mIsLiveStreaming && cache) {
            //options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
        }
        if (!mIsLiveStreaming) {
            //int startPos = getIntent().getIntExtra("start-pos", 0);
            //options.setInteger(AVOptions.KEY_START_POSITION, startPos * 1000);
        }
        mVideoPlayView.setAVOptions(options);

        // You can mirror the display
        // mVideoPlayView.setMirror(true);

        // You can also use a custom `MediaController` widget
//        VideoController mediaController = new VideoController(activity, !mIsLiveStreaming, mIsLiveStreaming);
//        mediaController.setOnClickSpeedAdjustListener(mOnClickSpeedAdjustListener);
//        mVideoPlayView.setMediaController(mediaController);

//        CustomVideoController videoController = new CustomVideoController(activity);
        mVideoPlayView.setMediaController(mVideoController);

        mVideoPlayView.setOnInfoListener(mOnInfoListener);
        mVideoPlayView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoPlayView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoPlayView.setOnCompletionListener(mOnCompletionListener);
        mVideoPlayView.setOnErrorListener(mOnErrorListener);
        //mVideoPlayView.setOnPreparedListener(mOnPreparedListener);
        //mVideoPlayView.setOnSeekCompleteListener(mOnSeekCompleteListener);

        mVideoPlayView.setLooping(false);

        mVideoPlayView.setVideoPath(videoPath);
    }

    //    @OnClick({R.id.img_fullscreen})
    //    public void onClick(View view) {
    //        switch (view.getId()) {
    //            case R.id.img_fullscreen:
    //                onFullscreen(view);
    //                break;
    //        }
    //    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoPlayView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoPlayView.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoPlayView.stopPlayback();
    }

    public void onScaleMode(View v) {
        mRotation = (mRotation + 90) % 360;
        mVideoPlayView.setDisplayOrientation(mRotation);
    }

    public void onFullscreen(View v) {
        mDisplayAspectRatio = (mDisplayAspectRatio + 1) % 5;
        mVideoPlayView.setDisplayAspectRatio(mDisplayAspectRatio);
        switch (mVideoPlayView.getDisplayAspectRatio()) {
            case PLVideoTextureView.ASPECT_RATIO_ORIGIN:
                showToastTips(activity, "Origin mode");
                break;
            case PLVideoTextureView.ASPECT_RATIO_FIT_PARENT:
                showToastTips(activity, "Fit parent !");
                break;
            case PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT:
                showToastTips(activity, "Paved parent !");
                break;
            case PLVideoTextureView.ASPECT_RATIO_16_9:
                showToastTips(activity, "16 : 9 !");
                break;
            case PLVideoTextureView.ASPECT_RATIO_4_3:
                showToastTips(activity, "4 : 3 !");
                break;
            default:
                break;
        }
    }

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    showToastTips(activity, "First video render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.i(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoPlayView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    showToastTips(activity, "IO Error !");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    showToastTips(activity, "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    showToastTips(activity, "failed to seek !");
                    break;
                default:
                    showToastTips(activity, "unknown error !");
                    break;
            }
            return true;
        }
    };

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            showToastTips(activity, "Play Completed !");
        }
    };

    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            Log.i(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
        }
    };

    private VideoController.OnClickSpeedAdjustListener mOnClickSpeedAdjustListener = new VideoController.OnClickSpeedAdjustListener() {
        @Override
        public void onClickNormal() {
            // 0x0001/0x0001 = 2
            mVideoPlayView.setPlaySpeed(0X00010001);
        }

        @Override
        public void onClickFaster() {
            // 0x0002/0x0001 = 2
            mVideoPlayView.setPlaySpeed(0X00020001);
        }

        @Override
        public void onClickSlower() {
            // 0x0001/0x0002 = 0.5
            mVideoPlayView.setPlaySpeed(0X00010002);
        }
    };

    private void updateStatInfo() {
        long bitrate = mVideoPlayView.getVideoBitrate() / 1024;
        final String stat = bitrate + "kbps, " + mVideoPlayView.getVideoFps() + "fps";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusView.setText(stat);
            }
        });
    }

    public void showToastTips(final Context context, final String tips) {
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show();
    }
}
