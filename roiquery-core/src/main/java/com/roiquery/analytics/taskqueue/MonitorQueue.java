package com.roiquery.analytics.taskqueue;

import static java.lang.Thread.sleep;

// 需要上报的场景，记录下
//1 上报gaid获取不到，检测是不是用户未授权
//2 检测用户是否重启了系统，或者修改了本地时间
//3 uploadData的每个子步骤的超时（现在定的是5秒）
//4 MainQueue任务执行耗时超过1秒
//5 所有串行队列是否卡死（1分钟无响应）
//6 DB的未上报记录超过100条
//7 uploadData连续3次执行失败

public class MonitorQueue extends AsyncTaskQueue {

    private volatile static MonitorQueue singleton;  //1:volatile修饰

    private volatile int mFlag = 1;

    private boolean isRunning = false;

    private AsyncTaskQueue watchQueue = null;
    public static MonitorQueue get() {
        if (singleton == null) {  //2:减少不要同步，优化性能
            synchronized (MonitorQueue.class) {  // 3：同步，线程安全
                if (singleton == null) {
                    singleton = new MonitorQueue();  //4：创建singleton 对象
                }
            }
        }
        return singleton;
    }

    private MonitorQueue() {
        super("MonitorQueue");
    }

    public void startMonitor(AsyncTaskQueue queue) {
        if (isRunning)
            return;

        isRunning = true;
        watchQueue = queue;
        postTask(new Runnable() {
                     @Override
                     public void run() {
                         while(isRunning) {
                             watchQueue.postTask(new Runnable() {
                                 @Override
                                 public void run() {
                                     MonitorQueue.get().setFlagExternal();
                                 }
                             });

                             try {
                                 Thread.sleep(60000);
                             } catch (InterruptedException ignored) {
                             }

                             if (mFlag == 1) {
                                 // do the report
                             } else {
                                 mFlag = 2;
                             }
                         }

                         watchQueue = null;
                     }
                 }
        );
    }

    public void stopMonitor() {
        isRunning = false;
    }

    public void setFlagExternal() {
        mFlag = 2;
    }
}
