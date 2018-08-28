package com.vmloft.develop.app.videoplayer.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.app.videoplayer.bean.VideoSimpleBean;
import com.vmloft.develop.library.tools.adapter.VMAdapter;
import com.vmloft.develop.library.tools.adapter.VMHolder;

import java.util.List;

/**
 * Create by lzan13 on 18/8/28 上午11:10
 */
public class HomeAdapter extends VMAdapter<VideoSimpleBean, HomeAdapter.HomeHolder> {

    public HomeAdapter(Context context, List<VideoSimpleBean> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_video, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        VideoSimpleBean simpleBean = getItemData(position);
        holder.titleView.setText(simpleBean.getTitle());
    }

    class HomeHolder extends VMHolder {
        public TextView titleView;

        public HomeHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_title);
        }
    }
}
