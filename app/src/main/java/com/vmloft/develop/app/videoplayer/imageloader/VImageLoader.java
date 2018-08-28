package com.vmloft.develop.app.videoplayer.imageloader;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.vmloft.develop.library.tools.VMActivity;

/**
 * Create by lzan13 on 2018/8/28
 */
public class VImageLoader {

    /**
     * 加载图片
     *
     * @param context
     * @param imgView
     * @param path
     * @param resId
     */
    public static void loadImage(Context context, ImageView imgView, String path, int resId) {
        GlideApp.with(context)
            .load(path)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(resId)
            .fitCenter()
            .into(imgView);
    }
}

