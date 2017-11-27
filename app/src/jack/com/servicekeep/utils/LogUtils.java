package jack.com.servicekeep.utils;

import android.content.Context;
import android.util.Log;


/**
 * log的工具类
 */
public class LogUtils {

    /**
     * todo 是否显示log日志，发版改成false
     */
    private static boolean sShowLog = true;


    public static void v(Context context, String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (sShowLog) {
            Log.d(tag, msg);
        }
    }

    public static void d(Context context, String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (sShowLog) {
            Log.i(tag, msg);
        }
    }

    public static void i(Context context, String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (sShowLog) {
            Log.e(tag, msg);
        }
    }

    public static void e(Context context, String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void e(Context context, String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void setLOG(Context context, boolean lOG) {
        sShowLog = lOG;
    }

}
