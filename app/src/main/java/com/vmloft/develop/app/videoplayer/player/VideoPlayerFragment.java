package com.vmloft.develop.app.videoplayer.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoDetailBean;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.imageloader.VImageLoader;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.widget.VMToast;

public class VideoPlayerFragment extends Fragment {

    private static final String TAG = "VideoPlayerFragment";
    private VMActivity mActivity;

    @BindView(R.id.view_video_player)
    PLVideoTextureView mVideoPlayView;
    @BindView(R.id.layout_loading)
    LinearLayout mLoadingLayout;
    @BindView(R.id.text_load)
    TextView mLoadView;
    @BindView(R.id.img_cover)
    ImageView mCoverView;
    @BindView(R.id.custom_video_controller)
    CustomController mController;

    private VideoDetailBean videoDetailBean;
    // 设置视频缩放尺寸，默认为原始尺寸
    private int mDisplayAspectRatio = PLVideoTextureView.ASPECT_RATIO_ORIGIN;

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

        initPLDroidPlayer();
    }

    /**
     * 初始化视频播放配置
     */
    private void initPLDroidPlayer() {
        AVOptions options = new AVOptions();
        // 打开视频时单次 http 请求的超时时间，一次打开过程最多尝试五次 单位为 ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 解码方式
        // codec＝AVOptions.MEDIA_CODEC_HW_DECODE，硬解
        // codec=AVOptions.MEDIA_CODEC_SW_DECODE, 软解
        // codec=AVOptions.MEDIA_CODEC_AUTO, 硬解优先，失败后自动切换到软解
        // 默认值是：MEDIA_CODEC_SW_DECODE
        int codec = AVOptions.MEDIA_CODEC_SW_DECODE; // 解码方式:
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);

        // 设置偏好的视频格式，设置后会加快对应格式视频流的打开速度，但播放其他格式会出错
        // m3u8 = 1, mp4 = 2, flv = 3
        options.setInteger(AVOptions.KEY_PREFER_FORMAT, 2);

        // 设置拖动模式，1 位精准模式，即会拖动到时间戳的那一秒；0 为普通模式，会拖动到时间戳最近的关键帧。默认为 0
        options.setInteger(AVOptions.KEY_SEEK_MODE, 1);

        // 设置日志级别
        int logLevel = 2;
        options.setInteger(AVOptions.KEY_LOG_LEVEL, logLevel);
        mVideoPlayView.setAVOptions(options);

        // 设置加载布局
        mVideoPlayView.setBufferingIndicator(mLoadingLayout);
        // 设置封面控件
        mVideoPlayView.setCoverView(mCoverView);
        // 画面预览模式，包括：原始尺寸、适应屏幕、全屏铺满、16:9、4:3
        // ASPECT_RATIO_ORIGIN
        // ASPECT_RATIO_FIT_PARENT
        // ASPECT_RATIO_PAVED_PARENT
        // ASPECT_RATIO_16_9
        // ASPECT_RATIO_4_3
        mVideoPlayView.setDisplayAspectRatio(mDisplayAspectRatio);

        mVideoPlayView.setVideoPath(videoDetailBean.getFile_url());
        mVideoPlayView.setLooping(false);

        // 设置视频播放控制器
        mController.setActivity(mActivity);
        mController.setTitle(videoDetailBean.getTitle());
        mController.initControllerListener(mVideoPlayView);
        mVideoPlayView.setMediaController(mController);

        VImageLoader.loadImage(mActivity, mCoverView, videoDetailBean.getPic_url(), R.drawable.img_placeholder);
    }

    @OnClick(R.id.img_scale_ratio)
    public void onClick() {
        mDisplayAspectRatio = (mDisplayAspectRatio + 1) % 5;
        mVideoPlayView.setDisplayAspectRatio(mDisplayAspectRatio);
        switch (mVideoPlayView.getDisplayAspectRatio()) {
            case PLVideoTextureView.ASPECT_RATIO_ORIGIN:
                VMToast.make("Origin mode").showDone();
                break;
            case PLVideoTextureView.ASPECT_RATIO_FIT_PARENT:
                VMToast.make("Fit parent !").showDone();
                break;
            case PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT:
                VMToast.make("Paved parent !").showDone();
                break;
            case PLVideoTextureView.ASPECT_RATIO_16_9:
                VMToast.make("16 : 9 !").showDone();
                break;
            case PLVideoTextureView.ASPECT_RATIO_4_3:
                VMToast.make("4 : 3 !").showDone();
                break;
            default:
                break;
        }
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
}