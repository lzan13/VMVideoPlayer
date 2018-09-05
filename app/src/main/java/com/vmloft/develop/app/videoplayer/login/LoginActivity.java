package com.vmloft.develop.app.videoplayer.login;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.home.MainActivity;
import com.vmloft.develop.app.videoplayer.widget.TextureVideoView;
import com.vmloft.develop.library.tools.VMActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create by lzan13 on 18/9/5 上午9:35
 */
public class LoginActivity extends VMActivity {

    @BindView(R.id.texture_video_view)
    TextureVideoView mVideoView;

    private AssetFileDescriptor mAssetFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(activity);

        init();
    }

    private void init() {
        try {
            mAssetFileDescriptor = getAssets().openFd("testvideo.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_login, R.id.btn_scale_center_crop, R.id.btn_scale_top, R.id.btn_scale_bottom})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(activity, MainActivity.class));
                break;
            case R.id.btn_scale_center_crop:
                mVideoView.stop();
                mVideoView.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);
                mVideoView.setDataSource(mAssetFileDescriptor);
                mVideoView.play();
                break;
            case R.id.btn_scale_top:
                mVideoView.stop();
                mVideoView.setScaleType(TextureVideoView.ScaleType.TOP);
                mVideoView.setDataSource(mAssetFileDescriptor);
                mVideoView.play();
                break;
            case R.id.btn_scale_bottom:
                mVideoView.stop();
                mVideoView.setScaleType(TextureVideoView.ScaleType.BOTTOM);
                mVideoView.setDataSource(mAssetFileDescriptor);
                mVideoView.play();
                break;
        }
    }
}
