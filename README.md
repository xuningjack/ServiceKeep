# ServiceKeep
Android Service保活模块（5.0以下使用c来fork进程，5.0以上使用KeepPushAliveJobSchedulerService来保活进程）  

### 具体调用:   
1、在WorkService的onStartCommand中执行要保活的操作业务。  
2、在初始化过程中调用KeepAliveManager.INSTANCE.startKeepAliveService(context);
3、停止服务调用KeepAliveManager.INSTANCE.stopKeepAliveSerice(context);
   

