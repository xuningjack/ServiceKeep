package jack.com.servicekeep.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.List;

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


    /**
     * 需要持续进行保活的操作
     */
    public void needKeepAlive(Context context) {
        if (!isServiceProcessRunning(context)) {
            LogUtils.d(TAG, "openPush -------- is not ServiceProcessRunning");
            startPushService(context);
        } else {
            LogUtils.d(TAG, "openPush -------- is ServiceProcessRunning");
            bindPushService(context);
        }
    }

    /**
     * 判断PushService进程是否在执行
     *
     * @return
     */
    private boolean isServiceProcessRunning(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        //check process list
        List<ActivityManager.RunningAppProcessInfo> processList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (TextUtils.equals(process.processName, Constant.PUSH_SERVICE_PROCESS_NAME)) {
                mPushServiceHostPackageName = process.pkgList[0];
                if (!PreferencesManager.getInstance().getHostAppPackageName(context).equals(mPushServiceHostPackageName)) {
                    PreferencesManager.getInstance().setHostAppPackageName(context, mPushServiceHostPackageName);
                }
                LogUtils.e(TAG, "PushService Already Running, Host PackageName : [" + mPushServiceHostPackageName + "]");
                return true;
            }
        }
        //check service list
        List<ActivityManager.RunningServiceInfo> serviceList = mActivityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : serviceList) {

            if (TextUtils.equals(service.process, Constants.PUSH_SERVICE_PROCESS_NAME) ||
                    TextUtils.equals(PushService.class.getName(), service.service.getClassName())) {
                mPushServiceHostPackageName = service.service.getPackageName();
                if (!TextUtils.equals(mPushServiceHostPackageName,
                        PreferencesManager.getInstance().getHostAppPackageName(context))) {
                    PreferencesManager.getInstance().
                            setHostAppPackageName(context, mPushServiceHostPackageName);
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
    private void startPushService(Context context) {
        WorkService.startService(context);
    }


    /**
     * 绑定Push Service
     */
    private void bindPushService(Context context) {
        try {
            //bind PushService
            Intent intent = new Intent();
            intent.setAction(mPushServiceHostPackageName +
                    Constants.PUSH_HOST_SERVICE_INTENT_ACTION_FLAG);
            intent.setPackage(mPushServiceHostPackageName);
            Intent explicitIntent = getExplicitIntent(mContext, intent);
            if (explicitIntent == null) {
                return;
            }
            context.bindService(explicitIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
     * 绑定PushService服务
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mPushService = IPushService.Stub.asInterface(iBinder);
                isBinded = true;
                appRegister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPushService = null;
            isBinded = false;
        }
    };


}
