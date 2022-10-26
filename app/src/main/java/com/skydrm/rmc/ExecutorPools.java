package com.skydrm.rmc;

import android.support.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * this class is designed as to hold all kinds of common thread pools that can be used by other class
 * <p>
 * Requirement:
 */
final public class ExecutorPools {
    static private int MINIMAL_COREPOOL_SIZE = 3;
    static private int MAXIMAL_POOL_SIZE = 8;
    static private int CPU_CORES_SIZE = Runtime.getRuntime().availableProcessors();
    static private int OPTIMIZED_POOL_SIZE = CPU_CORES_SIZE * 2 + 1;
    static private long KEEP_ALIVE_SEC = 5L;

    static private int MAX_SIZE_UI_POOL = 4;
    /**
     * only be used by UI directing tasks, other else which is directly fired by UI(I.E. background tasks) MUST avoid use it
     */
    static private ThreadPoolExecutor UI_INTENSIVE_POOL = new ThreadPoolExecutor(
            1,
            MAX_SIZE_UI_POOL,
            KEEP_ALIVE_SEC,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread t = new SkyDRMWorkerThread(r, "NEXTLABS_UI_INTENSIVE_POOL #" + mCount.getAndIncrement());
                    t.setPriority(Thread.MAX_PRIORITY);
                    return t;
                }
            }) {
        @Override
        public String toString() {
            return "FIRED_BY_UI " + super.toString();
        }
    };

    static private int MAX_SIZE_COMMON_POOL = OPTIMIZED_POOL_SIZE;
    /**
     * system common for background tasks
     * - size fixed: cpu_core*2+1
     */
    static private ThreadPoolExecutor COMMON_POOL = new ThreadPoolExecutor(
            MAX_SIZE_COMMON_POOL,
            MAX_SIZE_COMMON_POOL,
            KEEP_ALIVE_SEC,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(MAX_SIZE_COMMON_POOL),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new SkyDRMWorkerThread(r, "NEXTLABS_COMMON_POOL #" + mCount.getAndIncrement());
                }
            }) {
        @Override
        public String toString() {
            return "NETWORK_TASK " + super.toString();
        }
    };

    static private ThreadPoolExecutor CACHED_POOL = new ThreadPoolExecutor(
            OPTIMIZED_POOL_SIZE,
            OPTIMIZED_POOL_SIZE,
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new SkyDRMWorkerThread(r, "NEXTLABS_CACHED_POOL #" + mCount.getAndIncrement());
                }
            }) {
        @Override
        public String toString() {
            return "REGULAR_BACK_GROUND " + super.toString();
        }
    };

    /**
     * select a pool for Task to submit smartly
     * <p>
     * REGULAR_BACK_GROUND,     //low prior
     * NETWORK_TASK,            // med prior
     * FIRED_BY_UI              // high prior
     *
     * @return
     */
    static public synchronized ExecutorService SelectSmartly(Select_Type type) {
        int size;
        ExecutorService selected = CACHED_POOL; // by default
        switch (type) {
            case FIRED_BY_UI:
                // check if UI_INTENSIVE_POOL can be used
                size = UI_INTENSIVE_POOL.getPoolSize();
                if (size < MAX_SIZE_UI_POOL) {
                    selected = UI_INTENSIVE_POOL;
                } else {
                    size = COMMON_POOL.getActiveCount();
                    if (size < MAX_SIZE_COMMON_POOL) {
                        selected = COMMON_POOL;
                    } else {
                        selected = CACHED_POOL;
                    }
                }
                break;
            case NETWORK_TASK:
                size = COMMON_POOL.getActiveCount();
                if (size < MAX_SIZE_COMMON_POOL) {
                    selected = COMMON_POOL;
                }
                break;
            default:
                selected = CACHED_POOL;

        }
        SkyDRMApp.log.i("\nPool want: " + type + "\ngive: " + selected);
        return selected;
    }

    public enum Select_Type {
        REGULAR_BACK_GROUND,     //low prior
        NETWORK_TASK,            // med prior
        FIRED_BY_UI              // high prior

    }


    private static class SkyDRMWorkerThread extends Thread {
        public SkyDRMWorkerThread(Runnable runnable, String threadName) {
            super(runnable, threadName);
            setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    SkyDRMApp.log.e("CRITICAL UncaughtExceptionHandler in SkyDRMWorkerThread", ex);
                }
            });
        }
    }
}
