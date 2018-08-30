package com.vmloft.develop.app.videoplayer.common;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by lzan13 on 2018/8/30.
 */
public class VBrightness {

    /**
     * 判断是否开启了自动亮度调节
     */
    public static boolean isAutoBrightness(Context context) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 获取屏幕的亮度
     */
    public static int getScreenBrightness(Context context) {
        if (isAutoBrightness(context)) {
            return getAutoScreenBrightness(context);
        } else {
            return getManualScreenBrightness(context);
        }
    }

    /**
     * 获取手动模式下的屏幕亮度
     */
    public static int getManualScreenBrightness(Context context) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * 获取自动模式下的屏幕亮度
     */
    public static int getAutoScreenBrightness(Context context) {
        float nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            //[-1,1],无法直接获取到Setting中的值，以字符串表示
            nowBrightnessValue = Settings.System.getFloat(resolver, "screen_auto_brightness_adj");
        } catch (Exception e) {
            e.printStackTrace();
        }
        float tempBrightness = nowBrightnessValue + 1.0f; //[0,2]
        float fValue = (tempBrightness / 2.0f) * 225.0f;
        return (int) fValue;
    }

    /**
     * 设置亮度
     */
    public static void setBrightness(Activity activity, float brightness) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.screenBrightness = brightness * (1f / 255f);
            activity.getWindow().setAttributes(lp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 停止自动亮度调节
     */
    public static void stopAutoBrightness(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开启亮度自动调节
     *
     * @param context
     */
    public static void startAutoBrightness(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 保存亮度设置状态
     */
    public static void saveBrightness(Context context, int brightness) {
        try {
            Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
            // resolver.registerContentObserver(uri, true, myContentObserver);
            context.getContentResolver().notifyChange(uri, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
