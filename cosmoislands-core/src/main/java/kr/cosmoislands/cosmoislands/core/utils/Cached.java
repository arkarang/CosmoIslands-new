package kr.cosmoislands.cosmoislands.core.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class Cached<T> {

    private final Supplier<CompletableFuture<T>> supplier;
    private volatile T origin;
    private volatile long lastInvoked;
    private final long timeout;

    public Cached(T origin, Supplier<CompletableFuture<T>> supplier){
        this.origin = origin;
        this.supplier = supplier;
        this.lastInvoked = System.currentTimeMillis();
        this.timeout = 5000L;
    }

    public Cached(T origin, Supplier<CompletableFuture<T>> supplier, long timeout){
        this.origin = origin;
        this.supplier = supplier;
        this.lastInvoked = System.currentTimeMillis();
        this.timeout = timeout;
    }

    public T get(){
        try {
            long now = System.currentTimeMillis();
            boolean isOld = isOld(now);
            CompletableFuture<T> future = supplier.get();
            if (future.isDone() && !isOld) {
                return future.get();
            } else {
                future.thenAccept(this::set);
                return origin;
            }
        }catch (InterruptedException | ExecutionException e){
            return origin;
        }
    }

    private boolean isOld(long now){
        return now >= lastInvoked + timeout;
    }

    public synchronized void set(T newValue){
        this.lastInvoked = System.currentTimeMillis();
        this.origin = newValue;
    }

}
