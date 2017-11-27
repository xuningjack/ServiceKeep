package jack.com.servicekeep.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import jack.com.servicekeep.manager.ServiceManager;
import jack.com.servicekeep.utils.LogUtils;


/**
 * 保活Service
 *
 * @Author Jack
 * @Date 2017/9/26 15:17
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
@TargetApi(21)
public class KeepAliveJobSchedulerService extends JobService {

    private JobParameters mJobParameters;
    private final String TAG = "KeepAliveJobSchedulerService";
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // 返回true，表示该工作耗时，同时工作处理完成后需要调用jobFinished销毁
            LogUtils.d(TAG, "KeepAliveJobSchedulerService ------ onStartJob");
            mJobParameters = (JobParameters) msg.obj;
            if (mJobParameters != null) {
                LogUtils.d(TAG, "onStartJob params ---------- " + mJobParameters);
            }
            //执行需要保活的工作
            ServiceManager.INSTANCE.needKeepAlive(getApplicationContext());
            return true;
        }
    });



    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtils.d(TAG, "KeepAliveJobSchedulerService-----------onStartJob");
        Message message = Message.obtain();
        message.obj = params;
        mHandler.sendMessage(message);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtils.d(TAG, "KeepAliveJobSchedulerService-----------onStopJob");
        mHandler.removeCallbacksAndMessages(null);
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


}
