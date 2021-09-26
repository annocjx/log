package cx.turam.log;

/**
 * author: Administrator
 * created on: 2020/3/18 10:01
 * proj: Watcher
 * pkg: cx.turam.log
 * description:
 * 日志拦截器
 */
public interface Intercepter {
  public  boolean intercept(String tag,String msg);
}
