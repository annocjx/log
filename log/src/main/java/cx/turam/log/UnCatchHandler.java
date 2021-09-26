package cx.turam.log;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cx.turam.log.util.FileUtil;

/**
 * author: Administrator
 * created on: 26-Sep-19 16:43
 * proj: LCDApplication
 *  * pkg: com.mapsoft.lcd.util
 * description:
 */
public class UnCatchHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 记录activity栈
     */
   // private Application.ActivityLifecycleCallbacks lifecycleCallbacks;
   // private HashMap<String, WeakReference<Activity>> mHashMap = new HashMap<String, WeakReference<Activity>>();
   // private WeakReference<Activity> activity;


    private final String TAG = "UnCatchHandler";
    private Context app;
    private UnHandlerExListener mHandlerExListener;

    private UnCatchHandler() {
    }

    public static UnCatchHandler get(Context pApp) {
        return get(pApp, null);
    }

    public static UnCatchHandler get(Context pApp, UnHandlerExListener pListener) {
        UnCatchHandler ins = Holder.sUnCatchHandler;
        if (ins.app == null) {
            ins.app = pApp;
            ins.mHandlerExListener = pListener;
        }
        return ins;
    }


    private static class Holder {
        private static UnCatchHandler sUnCatchHandler = new UnCatchHandler();
    }

    /**
     * 上传app错误
     *
     * @param logInfo
     */
    public void uploadLog(String logInfo) {

    }

    /**
     * 上传接口错误，后面统一由框架层上报，应用侧不用上报网络错误，只上报app错误
     *
     * @param logInfo
     */
    public void uploadNetworkErrorLog(String logInfo) {

    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Thread.UncaughtExceptionHandler mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!handleException(thread, ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
          //  restartDelay(3000);
        }

    }

    private void restartDelay(final int delay) {
        //延时重新启动程序
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (Exception pE) {

                }
                //H.restartAppByAlarm(app, 3000);
                H.restartApplication(app);
            }
        }).start();

    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param pThread
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Thread pThread, Throwable ex) {
        if (ex == null) {
            return false;
        }
        //保存日志文件
        saveCrashInfo2File(ex, collectShprefs(app) + "\n" + collectDeviceInfo(app), getCauseStr(ex));
        //restartDelay(3000);
        return true;
    }

    private String collectShprefs(Context pApp) {
        StringBuilder sb = new StringBuilder();
        SharedPreferences sp = pApp.getSharedPreferences(pApp.getPackageName(), Context.MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : sp.getAll().entrySet()) {
            sb.append(entry.getKey() + ":" + entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private String collectDeviceInfo(Context ctx) {
        StringBuilder sb = new StringBuilder();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                sb.append("versionName=" + versionName).append("    ").append("versionCode=" + versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            L.e(app.getPackageName() + " an error occured when collect package info", e.toString());
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append("\n" + field.getName() + "=" + field.get(null).toString());
            } catch (Exception e) {
                L.e(TAG,app.getPackageName() + " an error occured when collect crash info", e.toString());
            }
        }
        return sb.toString();
    }

    private String getCauseStr(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        try {
            printWriter.close();
            writer.close();
            printWriter.flush();
            printWriter.close();
        } catch (IOException pE) {
         L.e(TAG,"获取unCaughtException详情时出错:"+pE.toString());
        }
        return writer.toString();
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @param deviceInfo
     * @param exInfo
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(final Throwable ex, String deviceInfo, final String exInfo) {
        final StringBuilder sb = new StringBuilder();
        sb.append(deviceInfo);
        sb.append("\n"+ exInfo);
        
        L.e(TAG,"saveCrashInfo2File:" + sb.toString());
        final String[] fileName = new String[1];
        final File[] file = new File[1];
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // long timestamp = System.currentTimeMillis();
                    String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE).format(new Date());
                    fileName[0] = time + ".log";
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String path = Environment.getExternalStorageDirectory() + "/mapsoft/log/";
                        File dir = new File(path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        if (dir.listFiles() != null && dir.listFiles().length > 20) {// 日志文件太多删除
                            FileUtil.deleteFilesInDir(dir);
                        }
                        synchronized (dir) {
                             file[0] = new File(path, fileName[0]);
                            if (!file[0].exists()) {
                                file[0].createNewFile();
                            }
                            FileOutputStream fos = new FileOutputStream(file[0]);
                            fos.write(sb.toString().getBytes());
                            fos.close();
                        }
                    }
                } catch (Exception e) {
                    L.e(TAG,app.getPackageName() + "an error occured while writing file...", e.toString());
                } finally {
                    if (mHandlerExListener != null) {
                        mHandlerExListener.handleEx(profile(exInfo.toString()),file[0],sb.toString());
                       /* try {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                
                      
                            EmailUtils.sendEmail(null, "LCD" + C.FP + App.getCustomer().label + C.FP
                                    + BuildConfig.VERSION_NAME, sb.toString());
                                }
                            }).start();
                        } catch (Exception pE) {
                            pE.printStackTrace();
                        }*/
                    }else {//默认的处理
                        restartDelay(3000);
                    }
                }

            }
        }).start();
        return fileName[0];
    }

    private String profile(String content){
        String regEx = "Caused by:(.*)";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(content);
        boolean rs = mat.find();
       if (rs) {
            return  mat.group(1);
       }else {
           return content.substring(0,content.indexOf("\n"));
       }
    }

}
