package tech.shutu.androidlibyuvimageutils.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;

/**
 * Created by raomengyang on 15/01/2017.
 */
public class SysUtil {

    public static final boolean ENABLE_LOG = true;
    public static final String TAG = "debug==>";
    public static final String KEY_RESULT_CODE = "result";
    public static final String KEY_REASON = "reason";
    public static final String O_TAG = "xxxxxxxxx, ";

    public static void rmyLog(String log) {
        if (log == null || !ENABLE_LOG) {
            return;
        }
        Log.e(TAG, log);
    }

    public static void o(String log) {
        rmyLog(O_TAG + log);
    }

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    public static void intoImmersiveMode(Activity instance) {
        int uiOptions = instance.getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiOptions &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
        uiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        instance.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
}
