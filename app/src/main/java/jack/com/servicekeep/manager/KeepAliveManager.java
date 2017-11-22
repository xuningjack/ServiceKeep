package jack.com.servicekeep.manager;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;


import jack.com.servicekeep.fork.NativeRuntime;
import jack.com.servicekeep.service.KeepPushAliveJobSchedulerService;
import jack.com.servicekeep.utils.FileUtils;
import jack.com.servicekeep.utils.LogUtils;

import static android.os.Build.VERSION_CODES.LOLLIPOP;





/**
 * 保活操作的工具类
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
     * 初始化并保活 监听服务PushService
     */
    public void startPushKeepAliveService(final Context context){
        if(context != null){
            if(Build.VERSION.SDK_INT < LOLLIPOP){   //5.0之前用c来fork进程
                LogUtils.d(TAG, "initService-----------c fork");
                String executable="libhelper.so";
                String aliasFile = "helper";
                final String serviceName = context.getPackageName() + "/com.wanmei.push.service.PushService";
                NativeRuntime.INSTANCE.runExecutable(context.getPackageName(), executable, aliasFile, serviceName);
                //开启service
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NativeRuntime.INSTANCE.startService(serviceName, FileUtils.createRootPath(context));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }else{   //5.0之后用jobscheduler来保活进程
                LogUtils.d(TAG, "initService-----------job scheduler");
                startJobScheduler(context);
            }
        }else{
            LogUtils.e(TAG, "initService context is null !!!!");
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startJobScheduler(Context context) {
        if (context != null) {
            LogUtils.d(TAG, "KeepAliveManager--------startJobScheduler");
            //开启唤醒PushService的定时任务
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
                    new ComponentName(context, KeepPushAliveJobSchedulerService.class));
            builder.setPeriodic(PERIOD);
            builder.setPersisted(true);   //需要权限RECEIVE_BOOT_COMPLETED

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
    }


}
