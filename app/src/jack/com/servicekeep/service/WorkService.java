package jack.com.servicekeep.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import jack.com.servicekeep.utils.LogUtils;






/**
 * 需要保活的业务服务
 * 完美世界
 * @Author Jack
 * @Date 2017/11/22 18:02
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
public class WorkService extends Service {

    private static final String TAG = "WorkService";
    private final static String ACTION_START = "action_start";


    /**
     * 停止服务
     * @param context
     */
    public static void stopservice(Context context){
        if(context != null){
            LogUtils.d(TAG, "WorkService ------- stopService");
            Intent intent = new Intent(context, WorkService.class);
            context.stopService(intent);
        }
    }

    /**
     * 开启PushService
     * @param context
     */
    public static void startService(Context context) {
        LogUtils.d(TAG, "WorkService ------- startService");
        if(context != null) {
            Intent intent = new Intent(context, WorkService.class);
            intent.setAction(ACTION_START);
            context.startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "WorkService -------   onBind");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //todo 启动子线程执行耗时操作
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                LogUtils.d(TAG, "WorkService ---------- onStartCommand Service工作了");
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "WorkService ------- is onDestroy!!!");
    }
}
