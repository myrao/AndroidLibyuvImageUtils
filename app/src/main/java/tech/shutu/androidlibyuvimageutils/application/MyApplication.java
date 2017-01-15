package tech.shutu.androidlibyuvimageutils.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by raomengyang on 15/01/2017.
 */

public class MyApplication extends Application {

    /**
     * so文件默认前缀带lib，在此引用时需要去掉"lib"和后缀".so"
     * */
    static {
        System.loadLibrary("yuv_utils");
        System.loadLibrary("yuv");
    }

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        initContext();
    }

    private void initContext() {
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
