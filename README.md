# ServiceKeep
Android Service保活模块（5.0以下使用c来fork进程，5.0以上使用JobService来保活进程）  

### 具体调用:   
1、在WorkService的onStartCommand中执行要保活的操作业务。  
2、在初始化过程中调用KeepAliveManager.INSTANCE.startKeepAliveService(context);  
3、停止服务调用KeepAliveManager.INSTANCE.stopKeepAliveSerice(context);  
4、配置AndroidManifest.xml，增加如下配置：  
&lt;!--增加唤醒WorkService的定时任务--&gt;    
&lt;service  
&emsp;&emsp;android:name="jack.com.servicekeep.service.KeepAliveJobSchedulerService"
&emsp;&emsp;android:persistent="true"
&emsp;&emsp;android:exported="false"
&emsp;&emsp;android:permission="android.permission.BIND_JOB_SERVICE"
&emsp;&emsp;android:process="jack.com.servicekeep.job_scheduler_service"/&gt;

&lt;!--todo 自己业务保活的Service，替换android:name为自己的业务类--&gt;    
&lt;service  
&emsp;&emsp;android:name="jack.com.servicekeep.service.WorkService"
&emsp;&emsp;android:exported="true"
&emsp;&emsp;android:persistent="true"
&emsp;&emsp;android:label="Jack"
&emsp;&emsp;android:process="com.jack.workservice"&gt;
&lt;/service&gt;


&lt;!--开机启动监听--&gt;  
&lt;uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" /&gt;