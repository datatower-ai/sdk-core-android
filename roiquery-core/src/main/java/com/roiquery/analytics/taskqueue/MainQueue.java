package com.roiquery.analytics.taskqueue;

import android.os.SystemClock;

import com.roiquery.analytics.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainQueue extends AsyncTaskQueue {

    private CountDownLatch syncLatch = new CountDownLatch(1);

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

    void taskWillRun() {
        // 网monitor线程抛送一个开始监听的逻辑

        MonitorQueue.get().postTask(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                long beginTime = SystemClock.elapsedRealtime();
                                                syncLatch.await(
                                                        5000,
                                                        TimeUnit.SECONDS
                                                );

//                                                long endTime = SystemClock.elapsedRealtime();
//                                                if ((endTime - beginTime) > 5000) {
//                                                    onTimeout();;
//                                                }

                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }

                                        void onTimeout() {
                                            LogUtils.e("Task run time out in main queue");
//                                            do the report
                                        }
                                    }
        );
    }

    void taskDidRun() {
        syncLatch.countDown();
    }
}
