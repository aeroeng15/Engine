package com.terraforged.core.concurrent.pool;

import com.terraforged.core.concurrent.LazyFuture;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.concurrent.batch.SyncBatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SingleThreadPool implements ThreadPool {

    private final SyncBatcher batcher = new SyncBatcher();

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return LazyFuture.adapt(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return new LazyFuture<>(callable);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Resource<Batcher> batcher() {
        return batcher;
    }
}
