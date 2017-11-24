package jack.com.servicekeep.manager;

import android.content.Context;
import android.content.SharedPreferences;

import jack.com.servicekeep.Constant;




/**
 * 共享参数操作工具类
 * @Author Jack
 * @Date 2017/11/24 17:24
 */
public enum PreferencesManager {

    INSTANCE;

    public static PreferencesManager getInstance() {
        return INSTANCE;
    }


    /**
     * 获取宿主的包名
     * @param context
     * @return
     */
    public String getHostAppPackageName(Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().
                getSharedPreferences(Constant.PREFERENCE_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.PREFERENCE_KEY_HOST_APP_PACKAGE_NAME, "");
    }


    /**
     * 保存宿主的包名
     * @param context
     * @param packageName
     */
    public void setHostAppPackageName(Context context, String packageName) {
        SharedPreferences sharedPreferences = context.getApplicationContext().
                getSharedPreferences(Constant.PREFERENCE_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.PREFERENCE_KEY_HOST_APP_PACKAGE_NAME, packageName);
        editor.commit();
    }

}
