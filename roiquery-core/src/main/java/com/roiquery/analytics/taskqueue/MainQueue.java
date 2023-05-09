package com.roiquery.analytics.taskqueue;

import android.os.SystemClock;

import com.roiquery.analytics.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainQueue extends AsyncTaskQueue {

    private volatile static MainQueue singleton;  //1:volatile修饰
    public static MainQueue get() {
        if (singleton == null) {  //2:减少不要同步，优化性能
            synchronized (MainQueue.class) {  // 3：同步，线程安全
                if (singleton == null) {
                    singleton = new MainQueue();  //4：创建singleton 对象
                }
            }
        }
        return singleton;
    }

    private MainQueue() {
        super("MainQueue");
    }
}
