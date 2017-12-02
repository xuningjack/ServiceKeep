package jack.com.servicekeep.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.List;

import jack.com.servicekeep.Constant;
import jack.com.servicekeep.service.WorkService;
import jack.com.servicekeep.utils.LogUtils;





/**
 * 需要保活Service的操作工具类
 * 完美世界
 *
 * @Author Jack
 * @Date 2017/11/22 18:08
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
public enum ServiceManager {

    INSTANCE;

    private final String TAG = "ServiceManager";

    /** 宿主的包名 */
    private String mPushServiceHostPackageName;
    /** 是否绑定了PushService服务 */
    private boolean isBinded;




    /**
     * 需要持续进行保活的操作
     */
    public void needKeepAlive(Context context) {
        if (!isServiceProcessRunning(context)) {
            LogUtils.d(TAG, "needKeepAlive -------- is not ServiceProcessRunning");
            startWorkService(context);
        } else {
            LogUtils.d(TAG, "needKeepAlive -------- is ServiceProcessRunning");
            bindWorkService(context);
        }
    }

    /**
     * 判断PushService进程是否在执行
     *
     * @return
     */
    private boolean isServiceProcessRunning(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> processList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (TextUtils.equals(process.processName, Constant.KEEP_ALIVE_SERVICE_PROCESS_NAME)) {
                mPushServiceHostPackageName = process.pkgList[0];
                if (!PreferencesManager.getInstance().getHostAppPackageName(context).equals(mPushServiceHostPackageName)) {
                    PreferencesManager.getInstance().setHostAppPackageName(context, mPushServiceHostPackageName);
                }
                LogUtils.e(TAG,
                        "PushService Already Running, Host PackageName : [" + mPushServiceHostPackageName + "]");
                return true;
            }
        }
        List<ActivityManager.RunningServiceInfo> serviceList = mActivityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : serviceList) {
            if (Constant.KEEP_ALIVE_SERVICE_PROCESS_NAME.equals(service.process) ||
                    TextUtils.equals(WorkService.class.getName(), service.service.getClassName())) {
                mPushServiceHostPackageName = service.service.getPackageName();
                if (!PreferencesManager.getInstance().getHostAppPackageName(context).equals(mPushServiceHostPackageName)) {
                    PreferencesManager.getInstance().setHostAppPackageName(context, mPushServiceHostPackageName);
                }
                LogUtils.e(TAG, "PushService Already Running, Host PackageName : [" +
                        mPushServiceHostPackageName + "]");
                return true;
            }
        }
        return false;
    }


    /**
     * 开启Push Service
     */
    private void startWorkService(Context context) {
        WorkService.startService(context);
    }


    /**
     * 绑定Push Service
     */
    private void bindWorkService(Context context) {
        try {
            //bind PushService
            Intent intent = new Intent();
            intent.setAction(mPushServiceHostPackageName + Constant.PUSH_HOST_SERVICE_INTENT_ACTION_FLAG);
            intent.setPackage(mPushServiceHostPackageName);
            Intent explicitIntent = getExplicitIntent(context, intent);
            if (explicitIntent == null) {
                return;
            }
            //context.bindService(explicitIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取启动PushService的Intent
     * @param context
     * @param implicitIntent
     * @return
     */
    private Intent getExplicitIntent(Context context, Intent implicitIntent) {
        try {
            // Retrieve all services that can match the given intent
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
            // Make sure only one match was found
            if (resolveInfo == null || resolveInfo.size() != 1) {
                return null;
            }
            // Get component info and create ComponentName
            ResolveInfo serviceInfo = resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            LogUtils.e(TAG, "getExplicitIntent packageName :" + packageName);
            LogUtils.e(TAG, "getExplicitIntent className :" + className);
            ComponentName component = new ComponentName(packageName, className);
            // Create a new intent. Use the old one for extras and such reuse
            Intent explicitIntent = new Intent(implicitIntent);
            // Set the component to be explicit
            explicitIntent.setComponent(component);
            return explicitIntent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 解绑PushService
     */
    private void unbindPushService() {
        if (isBinded) {
            //mPushService = null;
            isBinded = false;
        }
    }

}
