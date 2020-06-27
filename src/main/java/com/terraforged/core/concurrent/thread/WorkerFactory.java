package com.terraforged.core.concurrent.thread;

import com.terraforged.core.concurrent.thread.context.ContextThread;
import com.terraforged.core.concurrent.thread.context.ContextWorkerThread;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// As DefaultThreadPool but with custom thread names
public class WorkerFactory implements ThreadFactory {

    protected final String prefix;
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);

    public WorkerFactory(String name) {
        group = Thread.currentThread().getThreadGroup();
        prefix = name + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new ContextThread(group, task);
        thread.setDaemon(true);
        thread.setName(prefix + threadNumber.getAndIncrement());
        return thread;
    }

    public static class ForkJoin extends WorkerFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        public ForkJoin(String name) {
            super(name);
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = new ContextWorkerThread(pool);
            thread.setDaemon(true);
            thread.setName(prefix + threadNumber.getAndIncrement());
            return thread;
        }
    }
}
