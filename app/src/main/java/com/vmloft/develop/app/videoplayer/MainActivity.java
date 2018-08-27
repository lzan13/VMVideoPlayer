package com.vmloft.develop.app.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.vmloft.develop.app.videoplayer.bean.ResultBean;
import com.vmloft.develop.app.videoplayer.common.VCallback;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.network.NetHelper;
import com.vmloft.develop.app.videoplayer.player.VideoPlayerActivity;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.utils.VMLog;

public class MainActivity extends VMActivity {

    @BindView(R.id.edit_path)
    EditText pathEdit;

    private String videoPath = "http://videos.baoge.tv/Uploads/Download/2018-08-18/5b783644c399b.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(activity);

        init();
    }

    private void init() {
        pathEdit.setText(videoPath);
        NetHelper.getInstance().getVideoList(new VCallback() {
            @Override
            public void onDone(Object object) {
                ResultBean resultBean = (ResultBean) object;
                VMLog.i("请求成功 %s", resultBean.data.size());
            }

            @Override
            public void onError(int code, String desc) {
                VMLog.e("请求失败：cdoe: %d, desc: %s", code, desc);
            }
        });
    }

    @OnClick(R.id.btn_play)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                Intent intent = new Intent(activity, VideoPlayerActivity.class);
                intent.putExtra(VConstant.KEY_VIDEO_DETAIL, videoPath);
                startActivity(intent);
                break;
        }
    }
}
