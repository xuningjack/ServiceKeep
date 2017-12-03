#include <string.h>
#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/resource.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/wait.h>m
#include <pthread.h>
#include <android/log.h>


#define PROC_DIRECTORY "/proc/"
#define CASE_SENSITIVE    1
#define CASE_INSENSITIVE  0
#define EXACT_MATCH       1
#define INEXACT_MATCH     0
#define MAX_LINE_LEN 5

#define TAG "JackHelper"
//定义TAG
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)



/**
 * 执行命令
 */
void executeCommandWithPopen(char* command, char* out_result, int resultBufferSize) {
	FILE * fp;
	out_result[resultBufferSize - 1] = '\0';
	fp = popen(command, "r");
	if (fp) {
		fgets(out_result, resultBufferSize - 1, fp);
		out_result[resultBufferSize - 1] = '\0';
		pclose(fp);
	} else {
		LOGI("popen null,so exit");
		exit(0);
	}
}


/**
 * 检测服务，如果不存在服务则启动。
 * 通过am命令启动一个laucher服务，由laucher服务负责进行主服务的检测，laucher服务在检测后自动退出
 */
void checkAndRestartService(char* service) {
	LOGI("当前所在的进程pid=", getpid());
	char cmdline[200];
	//通过arm命令进行拉活
	sprintf(cmdline, "am startservice --user 0 -n %s", service);
	char tmp[200];
	//打印执行启动service的命令
	sprintf(tmp, "start cmd = %s", cmdline);
	executeCommandWithPopen(cmdline, tmp, 200);
	LOGI(tmp, TAG);
}


/**
 * 死循环监听WorkService
 */
void thread(char* srvname) {
	while(1){
	    LOGI("-----------------死循环监听WorkService");
		checkAndRestartService(srvname);
		sleep(30);
	}
}


/**
 * 启动服务，双进程守护
 * srvname  服务名
 * sdPath 之前创建子进程的pid写入的文件路径，缓存的根目录
 */
int startWorkService(char* srvname, char* sdPath) {
    //pthread_t用于声明线程ID
	pthread_t id;
	struct rlimit resourceLimit;

	/**
	 * 第一次fork的作用是让shell认为本条命令已经终止，不用挂在终端输入上。
	 * 还有一个作用是为后面setsid服务，setsid的调用者不能是进程组组长(group leader)，此时父进程是进程组组长。
	 */
	int pid = fork();
	if (pid < 0) {
		LOGI("first fork() error pid %d,so exit", pid);
		exit(0);
	} else if (pid > 0) {
		LOGI("first fork(): I'am father pid=%d", getpid());   //在unistd.h中
	} else { //  第一个子进程
		LOGI("first fork(): I'am child pid=%d", getpid());
		LOGI("first fork(): setsid=%d", setsid());
		// 使用umask修改文件的屏蔽字，为文件赋予跟=更多的权限。
		// 因为继承来的文件可能某些权限被屏蔽，从而失去某些功能，如读写
		umask(0);

		int pid = fork();
		if (pid == 0) { //第二个子进程
			FILE *filePid;  //生成一个文件叫pid
			sprintf(sdPath, "%s/pid", sdPath);
			if((filePid = fopen(sdPath, "a")) == NULL) {
			//a是以附加的方式打开只写文件。若文件不存在，则会建立该文件；
			//如果文件存在，写入的数据会被加到文件尾，即文件原先的内容会被保留。（EOF符保留）
				LOGI("%s文件还未创建!",sdPath);
				ftruncate(filePid, 0);   //改变文件的大小为0
				lseek(filePid, 0, SEEK_SET);  //移动文件指针到起始处
			}
			fclose(filePid);
			//换权限重新打开
			filePid = fopen(sdPath, "rw");   //打开成功返回指针
			if(filePid > 0){
				char buffTemp[6];
				//初始化buffTemp，全部清0
                //将buffTemp所指向的某一块内存中的内容全部设置为0
				memset(buffTemp, 0, sizeof(buffTemp));
				fseek(filePid, 0, SEEK_SET);  //文件指针回到文件起始处
				fgets(buffTemp, 6, filePid);     //读取一行
				LOGI("读取的进程号：%s", buffTemp);
				if(strlen(buffTemp) > 1){    // 有值
					//atoi把字符串转换成长整型
					kill(atoi(buffTemp), SIGTERM);
					LOGI("杀死进程，pid=%d", atoi(buffTemp));
				}
			}
			fclose(filePid);
			filePid = fopen(sdPath, "w");
			char buff[100];
			if(filePid > 0){
				sprintf(buff, "%lu", getpid());
				fprintf(filePid, "%s\n", buff);   //把进程号写入文件
				LOGI("把进程号写入文件。。。。。");
			}
			fclose(filePid);
			fflush(filePid);
			LOGI("step1 I'am child-child pid=%d", getpid());

			//step2: 修改进程工作目录为根目录，chdir("/").
			chdir("/");

			//step3:关闭不需要的从父进程继承过来的文件描述符。
			if (resourceLimit.rlim_max == RLIM_INFINITY) {
				resourceLimit.rlim_max = 1024;
			}
			int i;
			for (i = 0; i < resourceLimit.rlim_max; i++) {
				close(i);
			}

			umask(0);
			int ret = pthread_create(&id, NULL, (void *) thread, srvname);
			if (ret != 0) {
				printf("Create pthread error!\n");
				exit(1);
			}
			int stdfd = open ("/dev/null", O_RDWR);
			dup2(stdfd, STDOUT_FILENO);
			dup2(stdfd, STDERR_FILENO);
		} else {
			exit(0);
		}
	}
	return 0;
}


/**
 * jstring转String
 */
char* jstringTostring(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env, "java/lang/String");
	jstring strencode = (*env)->NewStringUTF(env, "utf-8");
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);
	jsize alen = (*env)->GetArrayLength(env, barr);
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0);
	return rtn;
}


/**
 * 启动Service
 */
void Java_jack_com_servicekeep_fork_NativeRuntime_startService(JNIEnv* env, jobject thiz,
		jstring processName, jstring sdpath) {
	char* rtn = jstringTostring(env, processName);     // 得到进程名称
	char* sd = jstringTostring(env, sdpath);
	LOGI("Java_jack_com_servicekeep_fork_NativeRuntime_startService run....ProcessName:%s", rtn);
	startWorkService(rtn, sd);
}


JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return result;
	}
	LOGI("JNI_OnLoad ......");
	return JNI_VERSION_1_4;
}
