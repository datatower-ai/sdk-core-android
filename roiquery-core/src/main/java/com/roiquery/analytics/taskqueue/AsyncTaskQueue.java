package com.roiquery.analytics.taskqueue;

import android.os.SystemClock;

import com.roiquery.analytics.utils.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncTaskQueue {
    private  String mName;

    private ThreadPoolExecutor mPool;

    protected long taskBeginTime = 0;
    protected long taskEndTime = 0;

    public void postTask(Runnable task) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                taskWillRun();
                task.run();
                taskDidRun();
            }
        });
    }

    void taskWillRun() {
        taskBeginTime = System.currentTimeMillis();
    }

    void taskDidRun() {
        taskEndTime = System.currentTimeMillis();
        if (taskEndTime - taskBeginTime > 3000) {
//             LogUtils.w();
        }
    }

    public int taskCount() {
        synchronized (mPool) {
            return mPool.getQueue().size();
        }
    }

    AsyncTaskQueue(String name) {
        mName = name;
        mPool = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryWithName(mName));
    }

    static class ThreadFactoryWithName implements ThreadFactory {

        private final String name;

        ThreadFactoryWithName(String name) {
            this.name = name;
        }

        public Thread newThread(Runnable r) {

            Thread thread = new Thread(r, name);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LogUtils.e(e.getMessage());
            });
            return thread;
        }
    };
}
