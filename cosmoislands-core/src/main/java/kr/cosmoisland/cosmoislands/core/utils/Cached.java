package kr.cosmoisland.cosmoislands.core.utils;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Cached<T> {

    private Supplier<CompletableFuture<T>> supplier;
    private volatile T origin;

    public Cached(T origin, Supplier<CompletableFuture<T>> supplier){
        this.origin = origin;
        this.supplier = supplier;
    }

    public T get(){
        try {
            CompletableFuture<T> future = supplier.get();
            if (future.isDone()) {
                return future.get();
            } else {
                future.thenAccept(this::set);
                return origin;
            }
        }catch (InterruptedException | ExecutionException e){
            return origin;
        }
    }

    public synchronized void set(T newValue){
        this.origin = newValue;
    }

}
