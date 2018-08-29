package com.vmloft.develop.app.videoplayer.home;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoSimpleBean;
import com.vmloft.develop.app.videoplayer.common.VCallback;
import com.vmloft.develop.app.videoplayer.common.VConstant;
import com.vmloft.develop.app.videoplayer.network.NetHelper;
import com.vmloft.develop.app.videoplayer.player.VideoPlayerActivity;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.adapter.VMAdapter;
import com.vmloft.develop.library.tools.utils.VMLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends VMActivity {

    private int page = 0;
    private int pageSize = 20;
    private boolean isFullscreen = false;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private RecyclerView.LayoutManager layoutManager;
    private HomeAdapter adapter;
    private List<VideoSimpleBean> videoSimpleBeanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(activity);

        init();
    }

    private void init() {
        layoutManager = new LinearLayoutManager(activity);
        adapter = new HomeAdapter(activity, videoSimpleBeanList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        setItemClickListener();
        requestVideoList();

    }

    /**
     * 设置列表项点击事件监听
     */
    private void setItemClickListener() {
        adapter.setItemClickListener(new VMAdapter.ICListener() {
            @Override
            public void onItemAction(int action, Object object) {
                VideoSimpleBean simpleBean = (VideoSimpleBean) object;
                startPlayer(simpleBean.getId());
            }

            @Override
            public void onItemLongAction(int action, Object object) {

            }
        });
    }

    /**
     * 请求视频数据列表
     */
    private void requestVideoList() {
        NetHelper.getInstance().requestVideoList(new VCallback() {
            @Override
            public void onDone(Object object) {
                List<VideoSimpleBean> list = (List<VideoSimpleBean>) object;
                refresh(list);
                VMLog.i("请求成功 %d", list.size());
            }

            @Override
            public void onError(int code, String desc) {
                VMLog.e("请求失败: %d, %s", code, desc);
            }
        });
    }

    private void refresh(final List<VideoSimpleBean> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoSimpleBeanList.clear();
                videoSimpleBeanList.addAll(list);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 去播放
     *
     * @param videoId
     */
    private void startPlayer(String videoId) {
        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra(VConstant.KEY_VIDEO_ID, videoId);
        startActivity(intent);
    }


    @OnClick({R.id.btn_rotate})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rotate:
                rotateUI();
                break;
        }
    }

    /**
     * 旋转 UI
     */
    private void rotateUI() {
        if (isFullscreen) {
            isFullscreen = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            isFullscreen = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            VMLog.i("屏幕方向变化，当前为竖屏模式");
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            VMLog.i("屏幕方向变化，当前为横屏模式");
        } else {
            VMLog.i("屏幕方向变化，不知道当前什么模式");
        }
    }
}
