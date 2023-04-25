package com.roiquery.analytics.taskqueue;


public class DataUploadQueue extends AsyncTaskQueue {

    private volatile static DataUploadQueue singleton;  //1:volatile修饰
    public static DataUploadQueue get() {
        if (singleton == null) {  //2:减少不要同步，优化性能
            synchronized (DataUploadQueue.class) {  // 3：同步，线程安全
                if (singleton == null) {
                    singleton = new DataUploadQueue();  //4：创建singleton 对象
                }
            }
        }
        return singleton;
    }

    private DataUploadQueue() {
        super("DataUploadQueue");
    }
}
