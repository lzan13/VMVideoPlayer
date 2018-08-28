package com.vmloft.develop.app.videoplayer.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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

public class MainActivity extends VMActivity {

    private int page = 0;
    private int pageSize = 20;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HomeAdapter adapter;
    private List<VideoSimpleBean> videoSimpleBeanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(activity);
        adapter = new HomeAdapter(activity, videoSimpleBeanList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        setItemClickListener();
        requestVideoList();
    }

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

    private void startPlayer(String videoId) {
        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra(VConstant.KEY_VIDEO_ID, videoId);
        startActivity(intent);
    }


}
