package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.ImmutableList;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class CosmoIslandFactory implements IslandFactory {

    private final ExecutorService service;
    private final IslandRegistrationDataModel model;
    private final IslandCloud cloud;
    private final boolean isLocal = true;

    private final LinkedList<String> orders = new LinkedList<>();
    private final ConcurrentHashMap<String, ComponentLifecycle> lifecycles = new ConcurrentHashMap<>();

    public synchronized void addFirst(String tag, ComponentLifecycle strategy) {
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            orders.addFirst(tag);
            lifecycles.put(tag, strategy);
        }

    }

    public synchronized void addLast(String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            orders.addLast(tag);
            lifecycles.put(tag, strategy);
        }
    }

    public synchronized void addBefore(String before, String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            int index = orders.indexOf(before);
            if(index == -1){
                throw new IllegalArgumentException(before+" is not exists");
            }else{
                if(index == 0){
                    orders.addFirst(tag);
                }else {
                    orders.add(index - 1, tag);
                }
                lifecycles.put(tag, strategy);
            }
        }

    }

    public synchronized void addAfter(String after, String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            int index = orders.indexOf(after);
            if(index == -1){
                throw new IllegalArgumentException(after+" is not exists");
            }else{
                if(index == orders.size()-1){
                    orders.addLast(tag);
                }else {
                    orders.add(index + 1, tag);
                }
                lifecycles.put(tag, strategy);
            }
        }
    }

    public ImmutableList<String> getOrders(){
        return ImmutableList.copyOf(orders);
    }

    public CompletableFuture<IslandContext> fireCreate(UUID uuid){
        CompletableFuture<IslandContext> contextFuture = model.create(uuid).thenComposeAsync(id-> {
            val updateStatusFuture = this.cloud.setStatus(id, IslandStatus.LOADING);
            return updateStatusFuture.thenApply(ignored-> new CosmoIslandContext(id, true));
        }, service);

        return contextFuture.thenApplyAsync(context->{
            DebugLogger.log("create current thread: "+Thread.currentThread().getName());
                for (ComponentLifecycle strategy : orderedList()) {
                    DebugLogger.log("island factory: execution on create: "+strategy.getClass().getSimpleName());
                    try {
                        strategy.onCreate(uuid, context).get(5000L, TimeUnit.MILLISECONDS);
                    } catch (Throwable ex) {
                        DebugLogger.error(ex);
                    }
                }
            DebugLogger.log("island factory: island creation completed: time: "+System.currentTimeMillis());
            return context;
        }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireLoad(int islandId, boolean isLocal) {
        CompletableFuture<Void> updateStatusFuture = CompletableFuture.completedFuture(null);
        if(isLocal) {
            updateStatusFuture = this.cloud.setStatus(islandId, IslandStatus.LOADING);
        }
        return CompletableFuture.supplyAsync(()->new CosmoIslandContext(islandId, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenApplyAsync(context->{
                    DebugLogger.log("load current thread: "+Thread.currentThread().getName());
                    for (ComponentLifecycle strategy : orderedList()) {
                        DebugLogger.log("island factory: execution on load: "+strategy.getClass().getSimpleName());
                        try{
                            strategy.onLoad(context).get(5000L, TimeUnit.MILLISECONDS);
                        }catch (Throwable ex){
                            DebugLogger.error(ex);
                        }
                    }
                    return context;
                }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireUnload(Island island) {
        CompletableFuture<Void> updateStatusFuture = CompletableFuture.completedFuture(null);
        if(island.isLocal()) {
            updateStatusFuture = this.cloud.setStatus(island.getId(), IslandStatus.UNLOADING);
        }
        return CompletableFuture.supplyAsync(() -> new CosmoIslandContext(island, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenApplyAsync(context->{
                    for (ComponentLifecycle strategy : orderedList()) {
                        try {
                            strategy.onUnload(context).get(5000L, TimeUnit.MILLISECONDS);
                        } catch (Throwable ex) {
                            DebugLogger.error(ex);
                        }
                    }
                    return context;
                }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireDelete(Island island) {
        val updateStatusFuture = this.cloud.setStatus(island.getId(), IslandStatus.UNLOADING);
        return CompletableFuture.supplyAsync(()->new CosmoIslandContext(island, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenComposeAsync(context->{
                    DebugLogger.log("delete current thread: "+Thread.currentThread().getName());

                        for (ComponentLifecycle strategy : orderedList()) {
                            DebugLogger.log("island factory: execution on delete: "+strategy.getClass().getSimpleName());
                            try {
                                strategy.onDelete(context).get(5000L, TimeUnit.MILLISECONDS);
                            } catch (Throwable ex) {
                                DebugLogger.error(ex);
                            }
                        }
                    return model.delete(island.getId()).thenApply(ignored->context);
                }, service);
    }

    private List<ComponentLifecycle> orderedList(){
        List<ComponentLifecycle> list = new ArrayList<>();
        for (String order : orders) {
            ComponentLifecycle strategy = lifecycles.get(order);
            if(strategy != null){
                list.add(strategy);
            }
        }
        return list;
    }
}
