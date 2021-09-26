package cx.turam.log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * author: Administrator
 * created on: 2019/11/9 15:34
 * proj: LCDApplication
 *  * pkg: com.mapsoft.lcd.service
 * description:
 */


public class PkgCatService extends Service {

    private static ExecutorService looperExecutor;
    public static final String SPACE_IDLE = "space_idle";
    private static float DEFAULT_OPT = 0.3f;
    public static final String SAVE_DIR = "L_dir";
    private final static String DEFAULT_SAVE_DIR = "/sdcard/L/";

    public static void start(Context pContext, float pOpt) {
        start(pContext, pOpt, DEFAULT_SAVE_DIR);
    }
 public static void start(Context pContext,  String saveDir) {
        start(pContext, DEFAULT_OPT, saveDir);
    }

    public static void start(Context pContext, float pOpt, String saveDir) {
        pContext.startService(
                new Intent(pContext, PkgCatService.class)
                        .putExtra(SPACE_IDLE, pOpt)
                        .putExtra(SAVE_DIR, saveDir)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (looperExecutor == null) {
            looperExecutor = new ThreadPoolExecutor(1, 1, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r, "PkgCatService");
                    if (thread.isDaemon()) {
                        thread.setDaemon(false);
                    }
                    return thread;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (looperExecutor != null) {
            looperExecutor.shutdownNow();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            looperExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    float opt = DEFAULT_OPT;
                    if (intent.hasExtra(SPACE_IDLE)) {
                        opt = intent.getFloatExtra(SPACE_IDLE, 0.3f);
                    } 
                    String saveDir = DEFAULT_SAVE_DIR;
                    if (intent.hasExtra(SAVE_DIR)) {
                        saveDir = intent.getStringExtra(SAVE_DIR);
                    }
                    //单个文件最大2m
                    long maxSize = 2L << 20;
                    LogcatUtil.startCatchLog(saveDir,opt,maxSize);
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

}

