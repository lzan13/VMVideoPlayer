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
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.imageloader.VImageLoader;
import com.vmloft.develop.library.tools.VMActivity;

public class VideoPlayerFragment extends Fragment {

    private static final String TAG = "VideoPlayerFragment";
    private VMActivity mActivity;

    @BindView(R.id.view_video_player) PLVideoTextureView mVideoPlayView;
    @BindView(R.id.layout_loading) LinearLayout mLoadingLayout;
    @BindView(R.id.img_cover) ImageView mCoverView;
    @BindView(R.id.custom_video_controller) CustomController mController;

    private VideoDetailBean videoDetailBean;

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

        initOptions();

        // 设置加载布局
        mVideoPlayView.setBufferingIndicator(mLoadingLayout);
        // 设置封面控件
        mVideoPlayView.setCoverView(mCoverView);
        // 设置视频播放的一些监听
        mVideoPlayView.setOnInfoListener(mOnInfoListener);
        mVideoPlayView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoPlayView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoPlayView.setOnCompletionListener(mOnCompletionListener);
        mVideoPlayView.setOnErrorListener(mOnErrorListener);

        mVideoPlayView.setVideoPath(videoDetailBean.getFile_url());
        mVideoPlayView.setLooping(false);

        // 设置视频播放控制器
        mVideoPlayView.setMediaController(mController);
        mController.setOnCtrlActionListener(new CustomController.OnCtrlActionListener() {
            @Override
            public void onAction(int action) {
                if (action == CustomController.ACTION_FULLSCREEN) {
                    ((VideoPlayerActivity) getActivity()).rotateUI();
                }
            }
        });
        mController.setTitle(videoDetailBean.getTitle());

        VImageLoader.loadImage(mActivity, mCoverView, videoDetailBean.getPic_url(), R.drawable.img_placeholder);
    }

    /**
     * 初始化视频播放配置信息
     */
    private void initOptions() {
        AVOptions options = new AVOptions();

        // 打开视频时单次 http 请求的超时时间，一次打开过程最多尝试五次 单位为 ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 解码方式
        // codec＝AVOptions.MEDIA_CODEC_HW_DECODE，硬解
        // codec=AVOptions.MEDIA_CODEC_SW_DECODE, 软解
        // codec=AVOptions.MEDIA_CODEC_AUTO, 硬解优先，失败后自动切换到软解
        // 默认值是：MEDIA_CODEC_SW_DECODE
        int codec = AVOptions.MEDIA_CODEC_AUTO; // 解码方式:
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);

        // 设置偏好的视频格式，设置后会加快对应格式视频流的打开速度，但播放其他格式会出错
        // m3u8 = 1, mp4 = 2, flv = 3
        options.setInteger(AVOptions.KEY_PREFER_FORMAT, 2);
        // 设置日志级别
        int logLevel = 2;
        options.setInteger(AVOptions.KEY_LOG_LEVEL, logLevel);
        mVideoPlayView.setAVOptions(options);
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