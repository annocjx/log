package cx.turam.log;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cx.turam.log.type.BaseLog;
import cx.turam.log.type.FileLog;
import cx.turam.log.type.JsonLog;
import cx.turam.log.type.XmlLog;
import cx.turam.log.util.CloseUtil;

/**
 * This is a Log tool，with this you can the following
 * <ol>
 * <li>use L.d(),you could print whether the method execute,and the default tag is current class's name</li>
 * <li>use L.d(msg),you could print log as before,and you could location the method with a click in Android Studio Logcat</li>
 * <li>use L.json(),you could print json string with well format automatic</li>
 * </ol>
 * <p>
  扩展功能，添加对文件的支持
  扩展功能，增加对XML的支持，修复BUG
  扩展功能，添加对任意参数的支持
  扩展功能，增加对无限长字符串支持
  扩展功能，添加对自定义全局Tag的支持,修复内部类不能点击跳转的BUG
  扩展功能，添加不能关闭的Log.debug(),用于发布版本的Log打印,优化部分代码
 * 16/6/20  扩展功能，添加堆栈跟踪功能Log.trace()
 */
public final class L {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String NULL_TIPS = "Log with null object";

    private static final String DEFAULT_MESSAGE = "execute";
    private static final String PARAM = "Param";
    private static final String NULL = "null";
    private static final String TAG_DEFAULT = "L";
    private static final String SUFFIX = ".java";

    public static final int JSON_INDENT = 4;

    public static final int V = 0x1;
    public static final int D = 0x2;
    public static final int I = 0x3;
    public static final int W = 0x4;
    public static final int E = 0x5;
    public static final int A = 0x6;

    private static final int JSON = 0x7;
    private static final int XML = 0x8;

    private static final int STACK_TRACE_INDEX_5 = 5;
    private static final int STACK_TRACE_INDEX_4 = 4;

    private static String mGlobalTag;
    private static boolean mIsGlobalTagEmpty = true;
    public static boolean OPEN = true;
    private static L sINSTANCE;

    private List<Intercepter> mIntercepters;

    public static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim());
    }

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static void setOPEN(boolean show) {
        OPEN = show;
    }

    public void startPkgCat(Context pContext) {
        startPkgCat(pContext,0.3f);
    }
    public void startPkgCat(Context pContext,float opt) {
        pContext.startService(new Intent(pContext, PkgCatService.class).putExtra(PkgCatService.SPACE_IDLE,opt));
    }

    public static L get(Context pApp) {
        return get(pApp, null);
    }

    public static L get(Context pApp, UnHandlerExListener pUnHandlerExListener) {
        if (sINSTANCE == null) {
            synchronized (L.class) {
                if (sINSTANCE == null) {
                    sINSTANCE = new L();
                    sINSTANCE.mIntercepters = new ArrayList<>(20);
                    Thread.setDefaultUncaughtExceptionHandler(UnCatchHandler.get(pApp, pUnHandlerExListener));
                }
            }
        }
        return sINSTANCE;
    }


    public void init(boolean isShowLog, @Nullable String tag, final UnHandlerExListener pListener) {
        OPEN = isShowLog;
        mGlobalTag = tag;
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag);
    }

    public void addIntercepter(Intercepter pIntercepter) {
        if (mIntercepters.indexOf(pIntercepter) == -1) {
            mIntercepters.add(pIntercepter);
        }
    }
    private boolean intercept(String tag,String content) {
        for (int i = 0; i < sINSTANCE.mIntercepters.size() ; i++) {
            Intercepter item = sINSTANCE.mIntercepters.get(i);
            if (item.intercept(tag,content)){
                return true;
            }
        }
        return false;
    }
    
    public void clearInterceptors(){
        if (sINSTANCE!=null&&sINSTANCE.mIntercepters!=null){
            sINSTANCE.mIntercepters.clear();
        }
    }
    public static void v() {
        printLog(V, null, DEFAULT_MESSAGE);
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... objects) {
        printLog(V, tag, objects);
    }

    public static void d() {
        printLog(D, null, DEFAULT_MESSAGE);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(String tag, Object... objects) {
        printLog(D, tag, objects);
    }

    public static void i() {
        printLog(I, null, DEFAULT_MESSAGE);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... objects) {
        printLog(I, tag, objects);
    }

    public static void w() {
        printLog(W, null, DEFAULT_MESSAGE);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... objects) {
        printLog(W, tag, objects);
    }

    public static void e() {
        printLog(E, null, DEFAULT_MESSAGE);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... objects) {
        printLog(E, tag, objects);
    }

    public static void a() {
        printLog(A, null, DEFAULT_MESSAGE);
    }

    public static void a(Object msg) {
        printLog(A, null, msg);
    }

    public static void a(String tag, Object... objects) {
        printLog(A, tag, objects);
    }

    public static void json(String jsonFormat) {
        printLog(JSON, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(JSON, tag, jsonFormat);
    }

    public static void xml(String xml) {
        printLog(XML, null, xml);
    }

    public static void xml(String tag, String xml) {
        printLog(XML, tag, xml);
    }

    public static void file(File targetDirectory, Object msg) {
        printFile(null, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, Object msg) {
        printFile(tag, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, String fileName, Object msg) {
        printFile(tag, targetDirectory, fileName, msg);
    }

    public static void debug() {
        printDebug(null, DEFAULT_MESSAGE);
    }

    public static void debug(Object msg) {
        printDebug(null, msg);
    }

    public static void debug(String tag, Object... objects) {
        printDebug(tag, objects);
    }

    public static void trace() {
        printStackTrace();
    }

    private static void printStackTrace() {
        if (!OPEN) {
            return;
        }
        Throwable tr = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        String message = sw.toString();

        String traceString[] = message.split("\\n\\t");
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (String trace : traceString) {
            if (trace.contains("at cx.turam.log.L")) {
                continue;
            }
            sb.append(trace).append("\n");
        }
        String[] contents = wrapperContent(STACK_TRACE_INDEX_4, null, sb.toString());
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        BaseLog.printDefault(D, tag, headString + msg);
        CloseUtil.close(sw,pw);
    }

   

    private static void printLog(int type, String tagStr, Object... objects) {
        if (!OPEN) {
            return;
        }
        String[] contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        if (sINSTANCE!=null&&sINSTANCE.intercept(tag+headString,msg)){
            return;
        }
        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
            case A:
                BaseLog.printDefault(type, tag, headString + msg);
                break;
            case JSON:
                JsonLog.printJson(tag, msg, headString);
                break;
            case XML:
                XmlLog.printXml(tag, msg, headString);
                break;
        }

    }

    private static void printDebug(String tagStr, Object... objects) {
        String[] contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        BaseLog.printDefault(D, tag, headString + msg);
    }


    private static void printFile(String tagStr, File targetDirectory, String fileName, Object objectMsg) {
        if (!OPEN) {
            return;
        }
        String[] contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objectMsg);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        if (sINSTANCE!=null&&sINSTANCE.intercept(tag+headString,msg)){
            return;
        }
        FileLog.printFile(tag, targetDirectory, fileName, headString, msg);
    }

    private static String[] wrapperContent(int stackTraceIndex, String tagStr, Object... objects) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement targetElement = stackTrace[stackTraceIndex];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }

        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String tag = (tagStr == null ? className : tagStr);

        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT;
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag;
        }

        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {
        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(NULL).append("\n");
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? NULL : object.toString();
        }
    }


    /**
     * 收集设备参数信息
     */
    private String collectDeviceInfo() {
        StringBuilder sb = new StringBuilder();
      /*  try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                sb.append("versionName=" + versionName).append("    ").append("versionCode=" + versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e("an error occured when collect package info", e.toString());
        }*/
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append("\n" + field.getName() + "=" + field.get(null).toString());
                d(field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                e("an error occured when collect crash info", e.toString());
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
        printWriter.close();
        return "\n" + writer.toString();
    }

    /* */

    /**
     * 当UncaughtException发生时会转入该函数来处理
     *//*
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e(TAG, e.getMessage());
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }*/
    public static void write2Local(String msg) {
        write2Local(msg, Environment.getExternalStorageDirectory().getAbsolutePath() + "/mapsoft/logs/");
    }

    public static void write2Local(String msg, String dir) {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        FileLog.printFile(null, TextUtils.isEmpty(dir) ? new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/mapsoft/logs/") :
                        new File(dir),
                today, "", msg + "\n");
    }

    public static String getLogTag(Class c) {
        return c.getSimpleName();
    }

    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }
}
