package com.terraforged.core.concurrent.cache;

public interface ExpiringEntry {

    long getTimestamp();

    default void close() {}
}
