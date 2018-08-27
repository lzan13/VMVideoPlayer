package com.vmloft.develop.app.videoplayer.player;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.vmloft.develop.app.videoplayer.Constant;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.VMActivity;

/**
 * Create by lzan13 on 2018/8/24
 */
public class VideoPlayerActivity extends VMActivity {

    private VideoPlayerFragment videoPlayerFragment;

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoPath = getIntent().getStringExtra(Constant.KEY_VIDEO_DETAIL);
        init();
    }

    private void init() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (videoPlayerFragment != null) {
            videoPlayerFragment = null;
        }
        videoPlayerFragment = VideoPlayerFragment.newInstance(videoPath);
        transaction.replace(R.id.fragment_video_container, videoPlayerFragment).commitAllowingStateLoss();
    }
}
