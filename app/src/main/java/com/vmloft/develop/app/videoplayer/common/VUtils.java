package com.vmloft.develop.app.videoplayer.common;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * Create by lzan13 on 2018/9/1
 */
public class VUtils {

    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;

    private volatile static boolean isCheckAllScreen = false;
    private volatile static boolean isAllScreen = false;

    private volatile static Point[] mRealSizes = new Point[2];

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        if (!isAllScreen(context)) {
            return getInnerScreenHeight(context);
        }
        return getInnerRealScreenHeight(context);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        if (!isAllScreen(context)) {
            return getInnerScreenWidth(context);
        }
        return getInnerRealScreenWidth(context);
    }

    /**
     * 获取真实高度
     */
    private static int getInnerRealScreenHeight(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getInnerScreenHeight(context);
        }

        int orientation = context.getResources().getConfiguration().orientation;
        orientation = orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;

        if (mRealSizes[orientation] == null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return getScreenHeight(context);
            }
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            mRealSizes[orientation] = point;
        }
        return mRealSizes[orientation].y;
    }

    /**
     * 获取真实宽度
     */
    private static int getInnerRealScreenWidth(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getInnerScreenWidth(context);
        }

        int orientation = context.getResources().getConfiguration().orientation;
        orientation = orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;

        if (mRealSizes[orientation] == null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return getInnerScreenWidth(context);
            }
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            mRealSizes[orientation] = point;
        }
        return mRealSizes[orientation].x;
    }

    /**
     * 获取高度
     */
    private static int getInnerScreenHeight(Context context) {
        if (context != null) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
        return 0;
    }

    /**
     * 获取宽度
     */
    private static int getInnerScreenWidth(Context context) {
        if (context != null) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
        return 0;
    }

    public static boolean isAllScreen(Context context) {
        if (isCheckAllScreen) {
            return isAllScreen;
        }
        isCheckAllScreen = true;
        isAllScreen = false;
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            if (height / width >= 1.97f) {
                isAllScreen = true;
            }
        }
        return isAllScreen;
    }
}
