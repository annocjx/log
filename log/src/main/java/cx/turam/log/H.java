package cx.turam.log;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cx.turam.log.util.CloseUtil;
import cx.turam.log.util.FileUtil;

/**
 * author: cx
 */

public class H {
    /**
     * ?????????????????????????????????
     */
    public static String getNowStr(Context c) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    /**
     * ???????????????????????????
     *
     * @param fileName :file
     * @return ??????????????????
     * @throws Exception
     */
    public static String codeString(File fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();
        String code = null;

        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        CloseUtil.closeQuietly(bin);
        return code;
    }

    /**
     * ????????????????????????,??????????????????response.body().bytes()
     *
     * @param inputStream
     */
    public static byte[] wrap(InputStream inputStream) throws IOException {

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff, 0, 1000)) > 0) {
            swapStream.write(buff, 0, len);
        }
        byte[] res = swapStream.toByteArray();
        inputStream.close();
        swapStream.close();
        return res;
    }


    //??????????????????:
    public static long UniwinGetTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                L.i(str2, num + "\t");
            }
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return initial_memory;
    }

    // ????????????????????????:
    public static long UniwinGetTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * ???????????????
     *
     * @param tag
     * @return
     */
    public static RuntimeException getStackTrace(String tag) {
        RuntimeException here = new RuntimeException("here");
        here.fillInStackTrace();
        Log.e(tag, "call stack: ", here);
        return here;
    }

    /**
     * ???????????????
     */
    public static void closeInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    /**
     * ???????????????
     */
    public static void openInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {//??????????????????
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);//?????????????????????
        }
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param mContext
     * @param serviceName ?????????+???????????????????????????net.loonggg.testbackstage.TestService???
     * @return true?????????????????????false??????????????????????????????
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * ?????????view?????????bitmap??????
     */
    public static Bitmap getViewBitmap(View pView, String subway, float pTextSize) {
        return getViewBitmap(pView);
    }

    public static Bitmap getViewBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * try get host activity from view.
     * views hosted on floating window like dialog     and toast will sure return null.
     *
     * @return host activity; or null if not available
     */
    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
    

    public static boolean hasEnglish(String str) {
        Pattern p = Pattern.compile("[a-zA-z]");
        if (p.matcher(str).find()) {
            return true;
        } else {
            return false;
        }
    }

    public static Spanned fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, 0);
        } else {
            return Html.fromHtml(text);
        }
    }

  /*  public static void openFeedback(Activity activity) {
        ShareCompat.IntentBuilder
                .from(activity)
                .setEmailTo(new String[]{"32161170174@qq.com"})
                .setSubject("LCD Feedback" + BuildConfig.VERSION_NAME)
                .setType("text/email")
                .setChooserTitle("Send LCD Feedback")
                .startChooser();
    }*/

    /**
     * @param aUrl    ??????
     * @param aEncode ??????
     * @return ?????????HTML??????
     */
    public static String getHTML(String aUrl, String aEncode) throws Exception {
        URL url = new URL(aUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            String htmlStr = new String(outStream.toByteArray(), aEncode);
            inputStream.close();
            outStream.close();
            return htmlStr;
        }
        return null;
    }


    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int dpToPx(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_MEDIUM);
        return Math.round(px);
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            java.lang.Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// ?????????
                    break;
                }
            }
        } catch (IOException ex) {
            // ???????????????
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static AnimatorSet addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", vaules)
                , ObjectAnimator.ofFloat(view, "scaleY", vaules)
        );
        set.setDuration(150);
        set.start();
        return set;
    }

    public static void addAnimation1(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                addAnimation(view),
                ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 0.7f, 0.5f, 0.8f, 1.0f, 1.1f, 1.25f, 1.5f, 1.9f, 1.9f, 1.9f, 1.0f)
        );
        set.setDuration(300);
        set.start();
    }

    //??????,????????????????????????
    public static void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        //??????
       /* ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        manager.restartPackage(context.getPackageName());*/
    }


    public static void restartAppByAlarm(Context context, long delay, Activity mainactivity) {
        Intent intent = new Intent(context, mainactivity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + delay, mPendingIntent);
        //System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * ??????????????????????????????,?????????????????????"???",??????????????????"???"
     *
     * @param lineName lineName
     * @return
     */
    public static String checkChineseChars0(String lineName) {
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(lineName);
        boolean hasChineseChar = false;
        while (m.find()) {
            hasChineseChar = true;
            break;
        }
        return hasChineseChar ? lineName : lineName + "???";
    }

    // ?????????????????????????????????
    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// ?????????????????????
    }

    // ???????????????????????????????????????
    public static String checkChineseChars(String lineName) {
        boolean has = false;
        for (char c : lineName.toCharArray()) {
            if (isChinese(c)) {
                has = true;// ??????????????????????????????
            }
        }
        return has ? lineName : lineName + "???";
    }


    private static final float DEVICE_DENSITY = Resources.getSystem().getDisplayMetrics().density;

    /**
     * ??????????????????????????? dp ????????? ????????? px(??????)
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        // return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
        return Math.round(dpValue * DEVICE_DENSITY);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * ??????mac??????,??????6.0??????
     */
    public static String getHardwareAddress() throws SocketException {
        String mac = null;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iF = interfaces.nextElement();
            byte[] addr = iF.getHardwareAddress();
            if (addr == null || addr.length == 0) {
                continue;
            }
            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            mac = buf.toString();
            Log.d("---mac", "interfaceName=" + iF.getName() + ", mac=" + mac);
        }
        return mac;
    }


    /**
     * ??????String???MD5???
     *
     * @param info ?????????
     * @return ???????????????MD5???
     */
    public static String getMD5(String info) {
        try {
            //?????? MessageDigest ?????????????????? MD5 ?????????????????????????????? MD5 ????????????????????? SHA1 ???????????????
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //update(byte[])????????????????????????
            //??????StringBuilder?????????append()?????????????????????????????????????????????????????????
            md5.update(info.getBytes("UTF-8"));
            //digest()????????????,MessageDigest???????????????????????????????????????????????????????????????????????????MD5????????????????????????reset()????????????????????????
            //digest()?????????16???????????????????????????byte[]??????
            BigInteger bi = new BigInteger(1, md5.digest());
            String value = bi.toString(16);
            return value;
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String getDateStr() {
        String str = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        return str;
    }


    //continue ?????????????????????????????????IPV6?????????????????????????????????IPV6??????????????????
    public static String getLocalIpAddress() {
        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress != null && !inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                    }
                    if (!TextUtils.isEmpty(ipAddress) && !ipAddress.contains("::")) {
                        return inetAddress.getHostAddress().toString();
                    } else {
                        continue;
                    }
                }

            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddress", ex.toString());
        }
        return "GetHostIP Fail,Please clear the shareReference";
    }


    public static Intent setStaticIpMode(Context c, boolean staticIp) {
        Intent intent = new Intent();
        if (staticIp) {
            intent.setAction("com.uniwin.set.static.ip");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); //?????????????????????????????????
            intent.putExtra("IpAddress", "192.168.100.90"); //??????ip????????????????????????yes???
            intent.putExtra("NetMask", "255.255.255.0"); //??????????????????????????????no
            intent.putExtra("GateWay", "192.168.100.100"); //??????????????????????????????no
            //intent.putExtra("Dns1", "192.168.1.1"); //??????dns????????????????????????no
            //intent.putExtra("Dns2", dns2); //??????dns????????????????????????no
        } else {

            //intent.setAction("action.ktv.net.receiver");
            //?????? 
//            intent.setAction("action.uniwin_net_mode");
//            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);//????????????
//            intent.putExtra("ip_setup_key", "dhcp");
            /*
            intent.setAction("com.uniwin.set.static.ip");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); //?????????????????????????????????
            intent.putExtra("IpAddress", "0.0.0.0"); //??????ip????????????????????????yes???
            intent.putExtra("NetMask", "0.0.0.0"); //??????????????????????????????no
            intent.putExtra("GateWay", "0.0.0.0"); //??????????????????????????????no
            */

            intent.setAction("action.uniwin_net_mode");
            intent.addFlags(Intent.FILL_IN_SOURCE_BOUNDS);
            intent.putExtra("ip_setup_key", "dhcp");

            /*Intent intent = new Intent();
            intent.setAction("action.uniwin_net_mode");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // ?????????????????????????????????
            intent.putExtra("ip_setup_key", "dhcp");
            context.sendBroadcast(intent);*/
        }
        c.sendBroadcast(intent);
        return intent;
    }


    //1????????????2????????????3?????????
    public static String getProviders(Context pContext) {
        int MB_ID = 0;
        String net = getNetWork(pContext);
        List<String> infos = getNetWorkList(pContext);
        if (net == null || net.equals("WIFI")) {
            if (infos.size() > 1) {
                infos.remove("WIFI");
                net = infos.get(0);
                if (net.equals("3gwap") || net.equals("uniwap")
                        || net.equals("3gnet") || net.equals("uninet")) {
                    MB_ID = 2;
                } else if (net.equals("cmnet") || net.equals("cmwap")) {
                    MB_ID = 1;
                } else if (net.equals("ctnet") || net.equals("ctwap")) {
                    MB_ID = 3;
                }
            } else {
                MB_ID = getProvidersName(pContext);
            }
        } else {
            if (net.equals("3gwap") || net.equals("uniwap")
                    || net.equals("3gnet") || net.equals("uninet")) {
                MB_ID = 2;
            } else if (net.equals("cmnet") || net.equals("cmwap")) {
                MB_ID = 1;
            } else if (net.equals("ctnet") || net.equals("ctwap")) {
                MB_ID = 3;
            }
        }
        switch (MB_ID) {
            case 1:
                return "??????";
            case 2:
                return "??????";
            case 3:
                return "??????";
            default:
                return "??????";
        }
    }

    public static List<String> getNetWorkList(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infos = cm.getAllNetworkInfo();
        List<String> list = new ArrayList<String>();
        if (infos != null) {
            for (int i = 0; i < infos.length; i++) {
                NetworkInfo info = infos[i];
                String name = null;
                if (info.getTypeName().equals("WIFI")) {
                    name = info.getTypeName();
                } else {
                    name = info.getExtraInfo();
                }
                if (name != null && list.contains(name) == false) {
                    list.add(name);
                    // System.out.println(name);
                }
            }
        }
        return list;
    }

    public static int getProvidersName(Context c) {
        int ProvidersName = 0;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) c
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telephonyManager.getSimOperator();
            if (operator == null || operator.equals("")) {
                operator = telephonyManager.getSubscriberId();
            }
            if (operator == null || operator.equals("")) {
                L.e("", "????????????sim?????????!");
            }
            if (operator != null) {
                if (operator.startsWith("46000")
                        || operator.startsWith("46002")) {
                    ProvidersName = 1;
                } else if (operator.startsWith("46001")) {
                    ProvidersName = 2;
                } else if (operator.startsWith("46003")) {
                    ProvidersName = 3;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProvidersName;
    }

    public static String getNetWork(Context c) {
        String NOWNET = null;
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            if (info.getTypeName().equals("WIFI")) {
                NOWNET = info.getTypeName();
            } else {
                NOWNET = info.getExtraInfo();// cmwap/cmnet/wifi/uniwap/uninet
            }
        }
        return NOWNET;
    }


    public static void drawTrangleFromCircle(Canvas pCanvas, Paint pPaint, float pX, float pY, double pV1, float pSpan) {
        drawTrangleFromCircle(pCanvas, pPaint, pX, pY, pV1, pSpan, 0);
    }

    /**
     * ???px,py?????????,?????????????????????????????????????????????
     *
     * @param pCanvas
     * @param pPaint
     * @param pX
     * @param pY
     * @param pV1
     * @param pSpan
     * @param pRotato
     */
    public static void drawTrangleFromCircle(Canvas pCanvas, Paint pPaint, float pX, float pY, double pV1, float pSpan, float pRotato) {
        boolean reverse = pRotato >= 180;
        Path path = new Path();
        if (reverse) {
            path.moveTo((float) (pX + 0.125f * pSpan),
                    (float) (pY + Math.sqrt(3) / 2f * pSpan / 8f));
            path.lineTo(pX, pY);
            path.lineTo((float) (pX + 0.125f * pSpan),
                    (float) (pY - Math.sqrt(3) / 2f * pSpan / 8f));
        } else {
            path.moveTo((float) (pX - 0.125f * pSpan),
                    (float) (pY + Math.sqrt(3) / 2f * pSpan / 8f));
            path.lineTo(pX, pY);
            path.lineTo((float) (pX - 0.125f * pSpan),
                    (float) (pY - Math.sqrt(3) / 2f * pSpan / 8f));
        }
        path.close();
        pCanvas.drawPath(path, pPaint);
    }

    public static String uniwinGetFirmwareVersion() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.product.firmware");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static void hideNaviBar(Context c) {
        c.sendBroadcast(new Intent("com.allwinner.hide_nav_bar"));
    }

    /**
     * ??????app????????????
     *
     * @return
     */
    private String getAppVersionName(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionName;
    }

    public long getCpuRate() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            Log.e("getCpuRate", "IOException" + ex.toString());
            return 0;
        }
        long totalCpu = 0;
        try {
            totalCpu = Long.parseLong(cpuInfos[2])
                    + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                    + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                    + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.i("getCpuRate", "ArrayIndexOutOfBoundsException" + e.toString());
            return 0;
        }
        return totalCpu;
    }
   

    /**
     * ???????????????????????????
     * ?????????UTF-8
     * ????????????GBK
     *
     * @param hexStr Byte?????????(Byte?????????????????? ???:[616C6B])
     * @return String ??????????????????
     */
    public static String hexStr2Str(String hexStr, String charset) {
        boolean doubleByte = false;

        byte[] bytes = new byte[hexStr.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            // ???????????????????????????????????????????????????????????????
            String a = hexStr.substring(i * 2, i * 2 + 2);
            int d = Integer.parseInt(a, 16);
            bytes[i] = (byte) d;// ??????e4???????????????????????????????????????
        }
        String goal = "";
        try {
            goal = new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return goal;

    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String versionName = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    /**
     * ???????????????hash???????????????????????????
     *
     * @param fileName
     * @param hashType
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getHash(String fileName, String hashType)
            throws IOException, NoSuchAlgorithmException {
        InputStream fis;
        fis = new FileInputStream(fileName);
        byte[] buffer = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }

    private static final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            sb.append(hexChar[(aB & 0xf0) >>> 4]);
            sb.append(hexChar[aB & 0x0f]);
        }
        return sb.toString();
    }

    public static byte[] UpcaseHexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (charToByte(achar[pos]) << 4 | charToByte(achar[pos + 1]));
        }
        //System.out.println(Arrays.toString(result)); 
        return result;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    private static Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save();
        float scaleSize = 1.0f;
        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }


    private void adjustVolume(Context c, boolean isVolumeUp) {
        AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (isVolumeUp) {
                audioManager.adjustSuggestedStreamVolume(
                        AudioManager.ADJUST_RAISE,
                        AudioManager.USE_DEFAULT_STREAM_TYPE,
                        AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            } else {
                audioManager.adjustSuggestedStreamVolume(
                        AudioManager.ADJUST_LOWER,
                        AudioManager.USE_DEFAULT_STREAM_TYPE,
                        AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
    }

    /**
     * ??????????????????(DBC case)<br/><br/>
     * ???????????????12288??????????????????32
     * ??????????????????(33-126)?????????(65281-65374)??????????????????????????????65248
     *
     * @return ???????????????
     * @para`m input ???????????????
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                //???????????????12288??????????????????32
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                //??????????????????(33-126)?????????(65281-65374)??????????????????????????????65248
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    private static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
      /*  if (res.activityInfo == null) {
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            return null;
        } else {
            return res.activityInfo.packageName;
        }*/
        return res.activityInfo == null || res.activityInfo.packageName.equals("android") ? null : res.activityInfo.packageName;
    }


    public static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    public static String perkBrackets(String stationName) {
        return stationName.replace("???", "???")
                .replace("???", "???")
                .replace("(", "???")
                .replace(")", "???");
    }

    public static Bitmap createRepeater(int slideWidth, Bitmap src) {
        int count = (slideWidth + src.getWidth() - 1) / src.getWidth(); //???????????????????????????width???????????????????????????????????????
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth() * count, src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int idx = 0; idx < count; ++idx) {
            canvas.drawBitmap(src, idx * src.getWidth(), 0, null);
        }
        return bitmap;
    }


    //???????????????
    public static String removeViceSiteName(String name) {
        if (name.contains("(") || name.contains("???")) {//???????????????
            return name.substring(0, !name.contains("(") ? name.indexOf("???") : name.indexOf("("));
        }
        return name;
    }

    public static String[] getfileFromAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return files;

    }

    /**
     * ??????????????????
     * ?????????(0 -- 255)??????
     */
    public static int getSystemScreenBrightness(Context context) {
        int values = 0;
        try {
            values = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return values;
    }

    /**
     * ??????????????????
     *
     * @param systemBrightness ???????????????????????????0-255?????????????????????
     */
    public static boolean setSystemScreenBrightness(Context context, int systemBrightness) {
        return Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, systemBrightness);
    }

    /**
     * ??????????????????????????????
     * return true ?????????????????????   return false ????????????????????????
     */
    public static boolean isAutoBrightness(Activity activity) {
        int autoBrightness = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        try {
            autoBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return autoBrightness == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    }

    /**
     * ??????????????????????????????
     */
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * ??????????????????????????????
     */
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * ??????????????????
     *
     * @param activity
     */
    public static void requireScreenOn(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * ??????????????????
     *
     * @param activity
     */
    public static void releaseScreenOn(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * ??????????????????
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        //  DisplayMetrics d = Resources.getSystem().getDisplayMetrics();
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(outMetrics);
        return (int) (outMetrics.heightPixels * outMetrics.density + 0.5f);
    }

    /**
     * ????????? ???+??? ?????? ?????????????????????????????????????????? ???-??? ??? ?????????
     */
    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        str = str.replace("\n", "").replace(",", "");
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * ??????????????????
     *
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(outMetrics);
        return (int) (outMetrics.widthPixels * outMetrics.density + 0.5f);
    }

    /**
     * ??????????????????
     *
     * @param activity
     * @return
     */
    public static int getScreenlength(Activity activity) {
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(outMetrics);
        return (int) (outMetrics.widthPixels * outMetrics.density + 0.5f);
    }


    public static File findFile(Context context, String fileFullName) {
        String str3 = context.getPackageCodePath();
        try {
            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(str3));
            ZipEntry currentZipEntry = null;
            while ((currentZipEntry = zipInput.getNextEntry()) != null) {
                String name = currentZipEntry.getName();
                if (!currentZipEntry.isDirectory()) {
                    Log.d("??????    ", name);
                    //  if( name.equalsIgnoreCase("AndroidManifest.xml")){
                    if (name.equalsIgnoreCase(fileFullName)) {
                        File file = new File(context.getFilesDir() + File.separator + name);
                        file.createNewFile();
                        // get the output stream of the file    
                        FileOutputStream out = new FileOutputStream(file);
                        int ch;
                        byte[] buffer = new byte[1024];
                        //read (ch) bytes into buffer    
                        while ((ch = zipInput.read(buffer)) != -1) {
                            // write (ch) byte from buffer at the position 0    
                            out.write(buffer, 0, ch);
                            out.flush();
                        }
                        out.close();
                        return file;
                    }
                }
            }
            zipInput.close();
        } catch (Exception e) {
            // TODO: handle exception  
        }
        return null;
    }


    public static String getAvailMemory(Context context) {// ??????android???????????????????????? 

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; ???????????????????????????
        return Formatter.formatFileSize(context, mi.availMem);// ????????????????????????????????? 
    }

    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// ???????????????????????? 
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// ??????meminfo????????????????????????????????? 

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                L.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]) * 1024;// ?????????????????????????????????KB?????????1024?????????Byte 
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte?????????KB??????MB???????????????????????? 
    }


    public static Bitmap capture(Activity activity) {
        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = activity.getWindow().getDecorView().getDrawingCache();
        return bmp;
    }


    /**
     * take snapshot. need phone to be rooted
     *
     * @param strPath the pic path to save
     * @return the real pic path
     * @throws IOException
     * @throws InterruptedException
     */
    public static String takeSnapShoot(String strPath) throws IOException, InterruptedException {
        if (TextUtils.isEmpty(strPath)) {
            strPath = String.format("/sdcard/%s.png", new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
        }
        Process sh = Runtime.getRuntime().exec("su", null, null);
        OutputStream os = sh.getOutputStream();
        os.write(("/system/bin/screencap -p " + strPath).getBytes("ASCII"));
        os.flush();
        os.close();
        sh.waitFor();
        return strPath;
    }


    //?????? 14.215.177.39
    public static boolean ping(String host, int pingCount, StringBuffer stringBuffer) {
        return ping(host, pingCount, stringBuffer, 10);
    }

    public static boolean ping(String host, int pingCount, StringBuffer stringBuffer, int deadline) {
        String line;
        Process process = null;
        BufferedReader successReader = null;
//        String command = "ping -c " + pingCount + " -w 5 " + host;
        String command = "ping -c " + pingCount + " " + host;//?????????????????????
        if (deadline > 0) {
            command = "ping -c " + pingCount + " -w " + deadline + " " + host;//?????????????????????s
        }
        boolean isSuccess = false;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                //     LogUtil.e("","ping fail:process is null.");
                append(stringBuffer, "ping fail:process is null.");
                return false;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                //   LogUtil.i(line);
                append(stringBuffer, line);
            }
            int status = process.waitFor();
            if (status == 0) {
                //LogUtil.i("exec cmd success:" + command);
                append(stringBuffer, "exec cmd success:" + command);
                isSuccess = true;
            } else {
                //  LogUtil.e("exec cmd fail.");
                append(stringBuffer, "exec cmd fail,status code:" + status);
                isSuccess = false;
            }
            //   LogUtil.i("exec finished.");
            append(stringBuffer, "exec finished.");
        } catch (IOException e) {
            //  LogUtil.e(e);
        } catch (InterruptedException e) {
            //  LogUtil.e(e);
        } finally {
            //   LogUtil.i("ping exit.");
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    // LogUtil.e(e);
                }
            }
        }
        return isSuccess;
    }

    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text).append("\n");
        }
    }
}
