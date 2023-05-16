package com.roiquery.analytics.taskqueue;

import androidx.annotation.NonNull;

import com.roiquery.analytics.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;

public class AsyncTaskQueue extends CoroutineDispatcher implements CoroutineScope {
    private String mName;

    private ThreadPoolExecutor mPool;

    public Thread activeThread;

    protected long taskBeginTime = 0;
    protected long taskEndTime = 0;

    static String tag = "PerfLog";

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
             LogUtils.d(tag, "task time out in queue" + this.mName);
        }
    }

    public int taskCount() {
        synchronized (mPool) {
            return mPool.getQueue().size();
        }
    }

    public Thread getCurrentThread() {
        return activeThread;
    }

    AsyncTaskQueue(String name) {
        mName = name;
        mPool = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryWithName(mName, this));
    }

    @Override
    public void dispatch(@NonNull CoroutineContext coroutineContext, @NonNull Runnable runnable) {
        postTask(runnable);
    }

    @NonNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return (CoroutineContext) this;
    }

    static class ThreadFactoryWithName implements ThreadFactory {

        private final String name;

        private WeakReference<AsyncTaskQueue> mHolder;

        ThreadFactoryWithName(String name, AsyncTaskQueue holder) {
            this.name = name;
            this.mHolder = new WeakReference(holder);
        }

        public Thread newThread(Runnable r) {

            Thread thread = new Thread(r, name);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LogUtils.e(e.getMessage());
            });
            mHolder.get().activeThread = thread;
            return thread;
        }
    };
}
