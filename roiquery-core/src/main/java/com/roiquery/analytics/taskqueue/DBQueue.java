package com.roiquery.analytics.taskqueue;

public class DBQueue extends AsyncTaskQueue {

    private volatile static DBQueue singleton;  //1:volatile修饰
    public static DBQueue get() {
        if (singleton == null) {  //2:减少不要同步，优化性能
            synchronized (DBQueue.class) {  // 3：同步，线程安全
                if (singleton == null) {
                    singleton = new DBQueue();  //4：创建singleton 对象
                }
            }
        }
        return singleton;
    }

    private DBQueue() {
        super("DBQueue");
    }
}
