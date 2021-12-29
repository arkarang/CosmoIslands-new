package kr.cosmoislands.cosmoislands.world;

import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CosmoWorldOperationRegistry implements WorldOperationRegistry {

    protected static class OperationsMap<T extends IslandWorldHandler>{

        private final WorldOperation<T> defaultOperation = (handler, settingsMap) -> CompletableFuture.completedFuture(true);
        private final ConcurrentHashMap<String, WorldOperation<T>> map = new ConcurrentHashMap<>();

        void register(String key, WorldOperation<T> operation){
            map.put(key, operation);
        }

        void unregister(String key){
            map.remove(key);
        }

        WorldOperation<T> get(String key){
            return map.get(key);
        }
    }

    private final ConcurrentHashMap<
            Class<? extends IslandWorldHandler>,
            OperationsMap<? extends IslandWorldHandler>> map;

    CosmoWorldOperationRegistry(){
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends IslandWorldHandler> void registerType(Class<T> clazz) {
        this.map.put(clazz, new OperationsMap<>());
    }

    @Override
    public <T extends IslandWorldHandler> void registerOperation(Class<T> clazz, String name, WorldOperation<T> operation) {
        OperationsMap<T> map = get(clazz);
        if(map != null){
            map.register(name, operation);
        }else{
            throw new IllegalArgumentException("the type "+clazz.getSimpleName()+" is not found");
        }
    }

    @Override
    public <T extends IslandWorldHandler> WorldOperation<T> getOperation(Class<T> clazz, String key) {
        OperationsMap<T> map = get(clazz);
        if(map != null){
            return map.get(key);
        }else{
            throw new IllegalArgumentException("the type "+clazz.getSimpleName()+" is not found");
        }
    }

    @SuppressWarnings("unchecked")
    <T extends IslandWorldHandler> OperationsMap<T> get(Class<T> clazz){
        return ( OperationsMap<T> )map.get(clazz);
    }
}
