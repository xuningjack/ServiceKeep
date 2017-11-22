package jack.com.servicekeep.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import jack.com.servicekeep.manager.ServiceManager;
import jack.com.servicekeep.utils.LogUtils;




/**
 * 保活Service
 * @Author Jack
 * @Date 2017/9/26 15:17
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
@TargetApi(21)
public class KeepPushAliveJobSchedulerService extends JobService {

    private JobParameters mJobParameters;
    private final String TAG = "KeepPushAliveJobSchedulerService";



    @Override
    public boolean onStartJob(JobParameters params) {
        // 返回true，表示该工作耗时，同时工作处理完成后需要调用jobFinished销毁
        mJobParameters = params;
        if(params != null){
            LogUtils.d(TAG, "onStartJob params ---------- " + mJobParameters);
        }
        //执行需要保活的工作
        ServiceManager.INSTANCE.needKeepAlive();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }




}
