package jack.com.servicekeep;

/**
 * 常量
 * 完美世界
 * @Author Jack
 * @Date 2017/11/24 16:22
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
public class Constant {

    /**
     * 要保活的Service的进程名
     */
    public static final String KEEP_ALIVE_SERVICE_PROCESS_NAME = "com.jack.service";

    /**
     * 启动保活Service的action
     */
    public static final String PUSH_HOST_SERVICE_INTENT_ACTION_FLAG = ".intent.action.JACK_SERVICE";


    public static final String PREFERENCE_DATA = "push_preference_data";
    public static final String PREFERENCE_KEY_HOST_APP_PACKAGE_NAME = "push_preference_host_package_name";


    public static class ResponseCode{

        /*
        * Push开启失败
        */
        public static final int PUSH_OPEN_FAIL_CODE = 101;
    }


    /**mqtt客户端的名称*/
    public static final String MQTT_CLIENT_NAME = "jackPush";

    /**开启push service*/
    public static final String ACTION_START = MQTT_CLIENT_NAME + ".START";
    /**停止push service*/
    public static final String ACTION_STOP = MQTT_CLIENT_NAME + ".STOP";
    /**重连mqtt*/
    public static final String ACTION_RECONNECT = MQTT_CLIENT_NAME + ".RECONNECT";
}
