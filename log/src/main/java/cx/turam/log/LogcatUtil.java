package cx.turam.log;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import cx.turam.log.util.CloseUtil;
import cx.turam.log.util.FileUtil;

/**
 * author: Administrator
 * created on: 2019/11/9 15:35
 * proj: LCDApplication
 * pkg: com.mapsoft.lcd.service
 * description:  Root情况下可以抓取其他应用的日志， 未Root时将只获得调用应用自身的Log
 */

public class LogcatUtil {


    private static boolean isRunning = false;

    public static void stopCatchLog() {
        isRunning = false;
    }

//    private static FileUtil logFileUtil = new LogFileUtil();


    /**
     * logcat *:E
     *
     * @param savePath savePath
     * @param maxSize  maxSize
     * @return maxSize
     */
    public static boolean startCatchLog(String savePath, float opt, long maxSize) {
        return startCatchLog("logcat -v time process |grep " + android.os.Process.myPid(), savePath, opt, maxSize);
    }


    /**
     * logcat *:E
     *
     * @param savePath savePath
     * @return boolean
     */
    public static boolean startCatchLog(String command, String savePath, float opt, long maxSize) {
        if (isRunning) {
            L.e("已经运行,command : " + command);
            return true;
        }
        if (TextUtils.isEmpty(savePath)) {
            L.e("日志保存目录为空");
            return false;
        }
        if (!FileUtil.SDCardEnabled()) {
            L.e("sd卡不可用");
            return false;
        }

        //按需清理历史记录
        File saveDir = new File(savePath);
        if (saveDir.exists() && saveDir.isFile()) {
            saveDir.delete();
        }
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        //清理后空间仍然不足,不写入日志
        if (FileUtil.checkDeleteHisFiles(opt, saveDir.getParent()) == -1) {
            return false;
        }


        int result = -1;
        if (TextUtils.isEmpty(command)) {
            return false;
        }
        isRunning = true;
        Process process = null;
        BufferedReader successReader = null;

        try {
            process = Runtime.getRuntime().exec("sh");
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.write(command.getBytes());
            outputStream.writeBytes("\n");
            outputStream.flush();
            CloseUtil.close(outputStream);
            String line;
            File lastSave = FileUtil.getLastFile(saveDir, H.getDateStr());

            while (isRunning) {
                //阻塞读取命令行log输出，不占用cpu时间片
                if ((line = successReader.readLine()) != null) {
                    //按需创建新文件                
                    if (lastSave.length() > maxSize) {
                        lastSave = new File(lastSave.getParent(), H.getDateStr());
                        L.v("切换日志文件:" +lastSave.getAbsolutePath());
                        if (lastSave.exists()) {
                            lastSave.delete();
                        }
                        lastSave.createNewFile();
                        if (FileUtil.checkDeleteHisFiles(opt, saveDir.getParent()) > -1.0F) {
                            FileUtil.writeStringToFile(lastSave, line + "\n", true);
                        } else {
                            L.a("LogCatUtil,存储空间较少,不再写入日志");
                            return false;
                        }
                    } else {
                        FileUtil.writeStringToFile(lastSave, line + "\n", true);
                    }
                }
            }
            isRunning = false;
        } catch (Exception e) {
            L.e("LogcatUtil", "写入日志出错" + e.toString());
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                CloseUtil.close(successReader);
//                if (successReader != null) {
//                    successReader.close();
//                }
            } catch (Exception e) {
                L.e("LogcatUtil", "process.destroy()出错:" + e.toString());
            }
        }
        return result == 0;
    }
}

