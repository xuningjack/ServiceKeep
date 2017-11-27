package jack.com.servicekeep.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import jack.com.servicekeep.utils.LogUtils;






/**
 * 需要保活的服务
 * 完美世界
 * @Author Jack
 * @Date 2017/11/22 18:02
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
public class WorkService extends Service {

    private static final String TAG = "WorkService";
    private final static String ACTION_START = "action_start";



    /**
     * 开启PushService
     * @param context
     */
    public static void startService(Context context) {
        LogUtils.d(TAG, "Jack is a good man!!  startService");
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "Jack is a good man!!  onBind");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //todo 启动子线程执行耗时操作
        LogUtils.d(TAG, "WorkService----------onStartCommand Service工作了");
        return START_STICKY;
    }



}
