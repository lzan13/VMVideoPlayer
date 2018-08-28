package com.vmloft.develop.app.videoplayer.player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnAudioFrameListener;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.imageloader.VImageLoader;
import com.vmloft.develop.library.tools.VMActivity;

import java.util.Arrays;

public class VideoPlayerFragment extends Fragment {

    private static final String TAG = "VideoPlayerFragment";
    private VMActivity mActivity;

    @BindView(R.id.view_video_player) PLVideoTextureView mVideoPlayView;
    @BindView(R.id.layout_loading) LinearLayout mLoadingLayout;
    @BindView(R.id.img_cover) ImageView mCoverView;
    @BindView(R.id.custom_video_controller) CustomVideoController mVideoController;

    private VideoDetailBean videoDetailBean;

    private boolean mIsLiveStreaming = false;

    public static VideoPlayerFragment newInstance(VideoDetailBean videoDetailBean) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(VConstant.KEY_VIDEO_DETAIL, videoDetailBean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoDetailBean = getArguments().getParcelable(VConstant.KEY_VIDEO_DETAIL);
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
        mActivity = (VMActivity) getActivity();
        init();
    }

    /**
     * 播放界面初始化
     */
    protected void init() {
        ButterKnife.bind(this, getView());

        mIsLiveStreaming = false;

        mVideoPlayView.setBufferingIndicator(mLoadingLayout);

        mVideoPlayView.setCoverView(mCoverView);

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = AVOptions.MEDIA_CODEC_AUTO;
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, mIsLiveStreaming ? 1 : 0);
        boolean disableLog = false;
        //        options.setString(AVOptions.KEY_DNS_SERVER, "127.0.0.1");
        options.setInteger(AVOptions.KEY_LOG_LEVEL, disableLog ? 5 : 0);
        boolean cache = false;
        if (!mIsLiveStreaming && cache) {
            //options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
        }
        //boolean vcallback = getIntent().getBooleanExtra("video-data-callback", false);
        //if (vcallback) {
        //    options.setInteger(AVOptions.KEY_VIDEO_DATA_CALLBACK, 1);
        //}
        //boolean acallback = getIntent().getBooleanExtra("audio-data-callback", false);
        //if (acallback) {
        //    options.setInteger(AVOptions.KEY_AUDIO_DATA_CALLBACK, 1);
        //}
        if (!mIsLiveStreaming) {
            //int startPos = getIntent().getIntExtra("start-pos", 0);
            //options.setInteger(AVOptions.KEY_START_POSITION, startPos * 1000);
        }
        mVideoPlayView.setAVOptions(options);

        // Set some listeners
        mVideoPlayView.setOnInfoListener(mOnInfoListener);
        mVideoPlayView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoPlayView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoPlayView.setOnCompletionListener(mOnCompletionListener);
        mVideoPlayView.setOnErrorListener(mOnErrorListener);

        mVideoPlayView.setVideoPath(videoDetailBean.getFile_url());
        mVideoPlayView.setLooping(false);

        // You can also use a custom `MediaController` widget
        mVideoPlayView.setMediaController(mVideoController);
        mVideoController.setTitle(videoDetailBean.getTitle());

        VImageLoader.loadImage(mActivity, mCoverView, videoDetailBean.getPic_url(), R.drawable.img_placeholder);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoPlayView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoPlayView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoPlayView.stopPlayback();
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
                break;
            case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
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
                //updateStatInfo();
                break;
            case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                Log.i(TAG, "Connected !");
                break;
            case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                Log.i(TAG, "Rotation changed: " + extra);
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
                Log.e(TAG, "IO Error!");
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

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            if (!mIsLiveStreaming) {
                //mVideoController.refreshProgress();
            }
            //finish();
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
}