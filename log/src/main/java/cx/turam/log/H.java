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
     * 获取特定格式的当前时间
     */
    public static String getNowStr(Context c) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    /**
     * 判断文件的编码格式
     *
     * @param fileName :file
     * @return 文件编码格式
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
     * 返回数据可能较长,不能直接使用response.body().bytes()
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


    //获取内存大小:
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

    // 获取存储空间大小:
    public static long UniwinGetTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 打印方法栈
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
     * 关闭软键盘
     */
    public static void closeInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    /**
     * 打开软键盘
     */
    public static void openInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {//软键盘已打开
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);//强制弹出软键盘
        }
    }


    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
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
     * 把一个view转化成bitmap对象
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
     * @param aUrl    网址
     * @param aEncode 编码
     * @return 返回的HTML代码
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
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
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

    //重启,串口初始化有问题
    public static void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        //或者
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
     * 包含中文字符的不处理,否则去掉其中的"路",再在末尾添加"路"
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
        return hasChineseChar ? lineName : lineName + "路";
    }

    // 判断一个字符是否是中文
    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    // 判断一个字符串是否含有中文
    public static String checkChineseChars(String lineName) {
        boolean has = false;
        for (char c : lineName.toCharArray()) {
            if (isChinese(c)) {
                has = true;// 有一个中文字符就返回
            }
        }
        return has ? lineName : lineName + "路";
    }


    private static final float DEVICE_DENSITY = Resources.getSystem().getDisplayMetrics().density;

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        // return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
        return Math.round(dpValue * DEVICE_DENSITY);
    }

    /**
     * 检测网络是否可用
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
     * 获取mac地址,兼容6.0以后
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
     * 获取String的MD5值
     *
     * @param info 字符串
     * @return 该字符串的MD5值
     */
    public static String getMD5(String info) {
        try {
            //获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //update(byte[])方法，输入原数据
            //类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
            md5.update(info.getBytes("UTF-8"));
            //digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
            //digest()返回值16位长度的哈希值，由byte[]承接
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


    //continue 很重要，不然会获得一个IPV6的地址，通过“：：”将IPV6地址过滤掉。
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
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); //必须加上，否则无法生效
            intent.putExtra("IpAddress", "192.168.100.90"); //设置ip地址，是否必填：yes；
            intent.putExtra("NetMask", "255.255.255.0"); //设置掩码，是否必填：no
            intent.putExtra("GateWay", "192.168.100.100"); //设置网关，是否必填：no
            //intent.putExtra("Dns1", "192.168.1.1"); //设置dns地址，是否必填：no
            //intent.putExtra("Dns2", dns2); //设置dns地址，是否必填：no
        } else {

            //intent.setAction("action.ktv.net.receiver");
            //或者 
//            intent.setAction("action.uniwin_net_mode");
//            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);//必须加上
//            intent.putExtra("ip_setup_key", "dhcp");
            /*
            intent.setAction("com.uniwin.set.static.ip");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); //必须加上，否则无法生效
            intent.putExtra("IpAddress", "0.0.0.0"); //设置ip地址，是否必填：yes；
            intent.putExtra("NetMask", "0.0.0.0"); //设置掩码，是否必填：no
            intent.putExtra("GateWay", "0.0.0.0"); //设置网关，是否必填：no
            */

            intent.setAction("action.uniwin_net_mode");
            intent.addFlags(Intent.FILL_IN_SOURCE_BOUNDS);
            intent.putExtra("ip_setup_key", "dhcp");

            /*Intent intent = new Intent();
            intent.setAction("action.uniwin_net_mode");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 必须加上，否则无法生效
            intent.putExtra("ip_setup_key", "dhcp");
            context.sendBroadcast(intent);*/
        }
        c.sendBroadcast(intent);
        return intent;
    }


    //1是移动，2是联通，3是电信
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
                return "移动";
            case 2:
                return "联通";
            case 3:
                return "电信";
            default:
                return "其他";
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
                L.e("", "未检测到sim卡信息!");
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
     * 以px,py为尖角,绘制表示沿基线的表示方向的箭头
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
     * 获取app当前版本
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
     * 十六进制转换字符串
     * 诸暨为UTF-8
     * 其它用户GBK
     *
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr, String charset) {
        boolean doubleByte = false;

        byte[] bytes = new byte[hexStr.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            // 将字符串每两个字符做为一个十六进制进行截取
            String a = hexStr.substring(i * 2, i * 2 + 2);
            int d = Integer.parseInt(a, 16);
            bytes[i] = (byte) d;// 将如e4转成十六进制字节，放入数组
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
     * 获取文件的hash值进行文件完整判断
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
     * 转半角的函数(DBC case)<br/><br/>
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     *
     * @return 半角字符串
     * @para`m input 任意字符串
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                //全角空格为12288，半角空格为32
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                //其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
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
        return stationName.replace("（", "︵")
                .replace("）", "︶")
                .replace("(", "︵")
                .replace(")", "︶");
    }

    public static Bitmap createRepeater(int slideWidth, Bitmap src) {
        int count = (slideWidth + src.getWidth() - 1) / src.getWidth(); //计算出平铺填满所给width（宽度）最少需要的重复次数
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth() * count, src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int idx = 0; idx < count; ++idx) {
            canvas.drawBitmap(src, idx * src.getWidth(), 0, null);
        }
        return bitmap;
    }


    //去除副站名
    public static String removeViceSiteName(String name) {
        if (name.contains("(") || name.contains("（")) {//中英文括号
            return name.substring(0, !name.contains("(") ? name.indexOf("（") : name.indexOf("("));
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
     * 获取系统亮度
     * 取值在(0 -- 255)之间
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
     * 设置系统亮度
     *
     * @param systemBrightness 返回的亮度值是处于0-255之间的整型数值
     */
    public static boolean setSystemScreenBrightness(Context context, int systemBrightness) {
        return Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, systemBrightness);
    }

    /**
     * 系统是否自动调节亮度
     * return true 是自动调节亮度   return false 不是自动调节亮度
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
     * 关闭系统自动调节亮度
     */
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 打开系统自动调节亮度
     */
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 请求屏幕常亮
     *
     * @param activity
     */
    public static void requireScreenOn(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 取消屏幕常亮
     *
     * @param activity
     */
    public static void releaseScreenOn(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 获取屏幕高度
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
     * 只能以 “+” 或者 数字开头；后面的内容只能包含 “-” 和 数字。
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
     * 获取屏幕宽度
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
     * 获取屏幕宽度
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
                    Log.d("文件    ", name);
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


    public static String getAvailMemory(Context context) {// 获取android当前可用内存大小 

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化 
    }

    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件 
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小 

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                L.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte 
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化 
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


    //百度 14.215.177.39
    public static boolean ping(String host, int pingCount, StringBuffer stringBuffer) {
        return ping(host, pingCount, stringBuffer, 10);
    }

    public static boolean ping(String host, int pingCount, StringBuffer stringBuffer, int deadline) {
        String line;
        Process process = null;
        BufferedReader successReader = null;
//        String command = "ping -c " + pingCount + " -w 5 " + host;
        String command = "ping -c " + pingCount + " " + host;//次数和等待时间
        if (deadline > 0) {
            command = "ping -c " + pingCount + " -w " + deadline + " " + host;//次数和等待时间s
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
