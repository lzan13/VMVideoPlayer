package com.vmloft.develop.app.videoplayer.player;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.vmloft.develop.app.videoplayer.Constant;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMDimen;

/**
 * Create by lzan13 on 2018/8/24
 */
public class VideoPlayerActivity extends VMActivity {

    private View playerContainer;
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
        playerContainer = findViewById(R.id.fragment_video_container);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int playerHeight = screenWidth * 720 / 1280;
        playerContainer.getLayoutParams().height = playerHeight;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (videoPlayerFragment != null) {
            videoPlayerFragment = null;
        }
        videoPlayerFragment = VideoPlayerFragment.newInstance(videoPath);
        transaction.replace(R.id.fragment_video_container, videoPlayerFragment).commitAllowingStateLoss();
    }
}
