package cx.turam.log.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import cx.turam.log.L;

/**
 * Desc:关闭IO流工具类
 */
public class CloseUtil {

    private CloseUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 关闭IO流
     *
     * @param closeables closeable
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                   L.e("关闭流出错:"+e.toString()+","+ Arrays.toString(closeables));
                }
            }
        }
    }

    /**
     * 安静关闭IO流,其实就是没有打印异常信息
     *
     * @param closeables closeable
     */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                    L.e("关闭流出错1:"+ignored.toString()+","+ Arrays.toString(closeables));
                }
            }
        }
    }
}
