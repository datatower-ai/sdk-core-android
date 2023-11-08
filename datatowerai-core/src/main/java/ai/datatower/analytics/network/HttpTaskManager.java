/*
 * Created by chenru on 2020/06/22.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.datatower.analytics.network;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ai.datatower.analytics.utils.LogUtils;


class HttpTaskManager {
    /**
     * 创建一个可重用固定线程数的线程池
     */
    private static final int POOL_SIZE = 1;

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private volatile static ExecutorService executor = null;

    private HttpTaskManager() {
    }

    private static ExecutorService getInstance() {
        if (executor == null) {
            synchronized (HttpTaskManager.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>(), new ThreadFactoryWithName("HttpTaskManager"));
                }
            }
        }
        return executor;
    }

    /**
     * 异步任务处理
     *
     * @param runnable 任务
     */
    static void execute(Runnable runnable) {
        getInstance().execute(runnable);
    }

    static class ThreadFactoryWithName implements ThreadFactory {

        private final String name;

        ThreadFactoryWithName(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, name);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LogUtils.e(e.getMessage());
            });
            return thread;
        }
    }
}
