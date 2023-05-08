package com.roiquery.analytics.taskqueue;

import static java.lang.Thread.sleep;

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
