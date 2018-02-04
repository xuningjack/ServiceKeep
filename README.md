# ServiceKeep
Android Service保活模块（5.0以下使用c来fork进程，5.0以上使用JobScheduler来保活进程）  

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

&lt;!--todo 自己业务保活的Service，重写WorkService中的onStartCommand其中执行自己的业务逻辑--&gt;     
&lt;service    
&emsp;&emsp;android:name="jack.com.servicekeep.service.WorkService"  
&emsp;&emsp;android:exported="true"  
&emsp;&emsp;android:persistent="true"  
&emsp;&emsp;android:label="Jack"  
&emsp;&emsp;android:process="com.jack.workservice"&gt;  
&lt;/service&gt;


&lt;!--开机启动监听--&gt;  
&lt;uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" /&gt;    

  
  
### 开发常见异常：
1、由于小米系统的深度定制，使用方需要：
设置---自启动管理---选择应用（如jack.com.servicekeep）---打开自启动即可。  
2、直接杀死什么进程，保活的是WorkService，不是demo。把demo干死了是不可以的，Android的底层机制。
