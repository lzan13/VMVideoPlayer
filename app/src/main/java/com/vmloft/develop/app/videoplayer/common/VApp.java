package com.vmloft.develop.app.videoplayer.common;

import android.content.Context;
import android.content.ContextWrapper;
import com.vmloft.develop.library.tools.VMActivity;
import com.vmloft.develop.library.tools.VMApp;

/**
 * Create by lzan13 on 18/8/28 上午11:43
 */
public class VApp extends VMApp {

    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    public static VMActivity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof VMActivity) {
            return (VMActivity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
