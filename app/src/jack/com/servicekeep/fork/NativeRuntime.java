package jack.com.servicekeep.fork;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;


/**
 * c层保活
 * @author Jack
 */
public enum NativeRuntime {

    INSTANCE;

    static {
        try {
            System.loadLibrary("helper");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行指定命令
     * @param packageName app包名
     * @param command 执行的命令
     * @param stringBuilder
     * @return
     */
    public boolean runLocalUserCommand(String packageName, String command, StringBuilder stringBuilder) {
        try {
            Process process = Runtime.getRuntime().exec("sh");   //获取shell进程
            DataInputStream inputStream = new DataInputStream(process.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("cd /data/data/" + packageName + "\n");     //在自己的数据目录里执行，才有权限写文件到当前目录
            outputStream.writeBytes(command + " &\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String s = new String(buffer);
            if (stringBuilder != null) {
                stringBuilder.append("CMD Result:\n" + s);
            }
        } catch (Exception e) {
            if (stringBuilder != null) {
                stringBuilder.append("Exception" + e.getMessage());
            }
            return false;
        }
        return true;
    }

    /**
     * 唤醒指定的进程
     * @param packageName 保活app的包名
     * @param soFileName so文件的名称
     * @param soFile 化名，如libhelper.so
     * @param serviceName 保活的服务名称
     * @return
     */
    public String runExecutable(String packageName, String soFileName, String soFile, String serviceName) {
        //path=  /data/data/包名
        String path = "/data/data/" + packageName;
        //cmd1=  /data/data/包名/lib/libhelper.so
        String cmd1 = path + "/lib/" + soFileName;
        //cmd2=  /data/data/包名/helper
        String cmd2 = path + "/" + soFile;
        //cmd2_a1=  /data/data/包名/helper mContext.getPackageName()+"/com.wanmei.push.service.PushService"
        String cmd2_a1 = path + "/" + soFile + " " + serviceName;
        //改变helper的属性,让其变为可执行,chmod 777 /data/data/包名/helper
        String cmd3 = "chmod 777" + cmd2;
        //拷贝lib/libhelper.so到上一层目录
        String cmd4 = "dd if=" + cmd1 + " of=" + cmd2;
        StringBuilder stringBuilder = new StringBuilder();
        if (!new File("/data/data/" + soFile).exists()) {
            runLocalUserCommand(packageName, cmd4, stringBuilder);
            stringBuilder.append(";");
        }
        runLocalUserCommand(packageName, cmd3, stringBuilder);
        stringBuilder.append(";");
        runLocalUserCommand(packageName, cmd2_a1, stringBuilder);
        stringBuilder.append(";");
        return stringBuilder.toString();
    }


    /**
     * 启动Service
     * @param srvname 启动服务的名称
     * @param sdpath
     */
    public native void startService(String srvname, String sdpath);

}
