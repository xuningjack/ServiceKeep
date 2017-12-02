package jack.com.servicekeep.manager;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.util.concurrent.TimeUnit;

import jack.com.servicekeep.fork.NativeRuntime;
import jack.com.servicekeep.service.KeepAliveJobSchedulerService;
import jack.com.servicekeep.service.WorkService;
import jack.com.servicekeep.utils.FileUtils;
import jack.com.servicekeep.utils.LogUtils;

import static android.app.job.JobScheduler.RESULT_SUCCESS;
import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;





/**
 * 保活操作的工具类
 *
 * @Author Jack
 */
public enum KeepAliveManager {

    INSTANCE;

    private final String TAG = "KeepAliveManager";
    private final int JOB_ID = 1;
    /**
     * 每隔1s中唤醒一次
     */
    private final int PERIOD = 1000;




    /**
     * 停止运行保活的服务
     * @param context
     */
    public void stopKeepAliveSerice(final Context context){
        WorkService.stopservice(context);
    }


    /**
     * 初始化并保活 监听服务PushService
     */
    public void startKeepAliveService(final Context context) {
        if (context != null) {
            if (Build.VERSION.SDK_INT < LOLLIPOP) {   //5.0之前用c来fork进程
                LogUtils.d(TAG, "initService-----------c fork");
                String executable = "libhelper.so";
                String aliasFile = "helper";
                final String serviceName = context.getPackageName() + "/jack.com.servicekeep.service.WorkService";
                LogUtils.d(TAG, "serviceName-----------" + serviceName);
                NativeRuntime.INSTANCE.runExecutable(context.getPackageName(), executable, aliasFile, serviceName);
                //开启service
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NativeRuntime.INSTANCE.startService(serviceName, FileUtils.createRootPath(context));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {   //5.0之后用jobscheduler来保活进程
                LogUtils.d(TAG, "initService-----------job scheduler");
                startJobScheduler(context);
            }
        } else {
            LogUtils.e(TAG, "initService context is null !!!!");
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startJobScheduler(Context context) {
        if (context != null) {
            LogUtils.d(TAG, "KeepAliveManager--------startJobScheduler");
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            //开启唤醒PushService的定时任务
            ComponentName componentName = new ComponentName("jack.com.servicekeep",
                    "jack.com.servicekeep.service.KeepAliveJobSchedulerService");
            //ComponentName componentName = new ComponentName(context, KeepAliveJobSchedulerService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName);
            builder.setPeriodic(PERIOD);
            builder.setPersisted(true);   //需要权限RECEIVE_BOOT_COMPLETED
            jobScheduler.schedule(builder.build());

            ComponentName jobService = new ComponentName(context.getPackageName(),
                    KeepAliveJobSchedulerService.class.getName());
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, jobService)
                    .setPeriodic(PERIOD)    //设置间隔时间
                    .setPersisted(true)
                    .setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR) //线性重试方案
                    .build();
            int result = jobScheduler.schedule(jobInfo);
            if (result == RESULT_SUCCESS) {
                LogUtils.d(TAG, "startJobScheduler ------ success!!!");
            }else{
                LogUtils.d(TAG, "startJobScheduler ------ fail!!!");
            }
        }
    }
}
