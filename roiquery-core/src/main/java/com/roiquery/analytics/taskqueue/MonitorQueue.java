package com.roiquery.analytics.taskqueue;

public class MonitorQueue extends AsyncTaskQueue {

    private volatile static MonitorQueue singleton;  //1:volatile修饰
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
}
