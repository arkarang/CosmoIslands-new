package kr.cosmoislands.cosmoislands.world;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.minepalm.manyworlds.api.ManyWorld;
import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import kr.cosmoislands.cosmoislands.core.utils.Cached;
import lombok.Getter;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

//todo:
// 1. create remoted IslandWorld
// - remoted island doesnt access IslandWorld data that causing NPE & broken synchronization
// 2. make enum world coordination properties like maxX, minZ
public class IslandManyWorld implements IslandWorld {

    private static final String MAX_X = "maxX", MIN_X = "minX", MAX_Z = "maxZ", MIN_Z = "minZ", LENGTH = "LENGTH", WEIGHT = "WEIGHT";

    private final int islandId;
    private final Map<String, Integer> initialValues;
    @Getter
    private final ManyWorld manyWorld;
    private final MySQLIslandWorldDataModel model;
    private final IslandWorldHandler handler;
    private final Cached<Integer> maxX, minX;
    private final Cached<Integer> maxZ, minZ;
    private final Cached<Integer> length, weight;
    private final Map<String, Supplier<CompletableFuture<Integer>>> supplierMap;
    private final LoadingCache<String, CompletableFuture<Integer>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, CompletableFuture<Integer>>() {
                @Override
                public CompletableFuture<Integer> load(String key) throws Exception {
                    return supplierMap.get(key).get();
            }
    });

    IslandManyWorld(int islandId,
                    ManyWorld world,
                    IslandWorldHandler handler,
                    MySQLIslandWorldDataModel model,
                    Map<String, Integer> initialValues){
        this.islandId = islandId;
        this.manyWorld = world;
        this.initialValues = initialValues;
        this.model = model;
        this.handler = handler;
        this.supplierMap = initSuppliers();
        this.maxX = new Cached<>(initialValues.get(MAX_X), supply(MAX_X));
        this.minX = new Cached<>(initialValues.get(MIN_X), supply(MIN_X));
        this.maxZ = new Cached<>(initialValues.get(MAX_Z), supply(MAX_Z));
        this.minZ = new Cached<>(initialValues.get(MIN_Z), supply(MIN_Z));
        this.length = new Cached<>(initialValues.get(MAX_X)-initialValues.get(MIN_X), supply(LENGTH));
        this.weight = new Cached<>(initialValues.get(MAX_Z)-initialValues.get(MIN_Z), supply(WEIGHT));
    }

    private Map<String, Supplier<CompletableFuture<Integer>>> initSuppliers(){
        HashMap<String, Supplier<CompletableFuture<Integer>>> map = new HashMap<>();
        map.put(MAX_X, ()->model.getMaxX(islandId));
        map.put(MIN_X, ()->model.getMinX(islandId));
        map.put(MAX_Z, ()->model.getMaxZ(islandId));
        map.put(MIN_Z, ()->model.getMinZ(islandId));
        map.put(LENGTH, ()->model.getLength(islandId, this.getMaxX() - this.getMinX()));
        map.put(WEIGHT, ()->model.getWeight(islandId, this.getMaxZ() - this.getMinZ()));
        return ImmutableMap.copyOf(map);
    }

    private Supplier<CompletableFuture<Integer>> supply(String key){
        return ()-> {
            try {
                return cache.get(key);
            }catch (ExecutionException e){
                return CompletableFuture.completedFuture(initialValues.get(key));
            }
        };
    }

    @Override
    public IslandWorldHandler getWorldHandler() {
        return handler;
    }

    @Override
    public int getMaxX() {
        return maxX.get();
    }

    @Override
    public int getMaxZ() {
        return maxZ.get();
    }

    @Override
    public int getMinX() {
        return minX.get();
    }

    @Override
    public int getMinZ() {
        return minZ.get();
    }

    @Override
    public CompletableFuture<Void> setBorder(AbstractLocation min, AbstractLocation max) {
        int minX, minZ;
        int maxX, maxZ;
        minX = (int)Math.min(min.getX(), max.getX());
        minZ = (int)Math.min(min.getZ(), max.getZ());
        maxX = (int)Math.max(min.getX(), max.getX());
        maxZ = (int)Math.max(min.getZ(), max.getZ());
        val future1= model.setMaxX(islandId, maxX);
        val future2= model.setMaxZ(islandId, maxZ);
        val future3= model.setMinX(islandId, minX);
        val future4= model.setMinZ(islandId, minZ);
        this.maxX.set(maxX);
        this.maxZ.set(maxZ);
        this.minX.set(minX);
        this.minZ.set(minZ);
        this.length.set(maxX - minX);
        this.weight.set(maxZ - minZ);
        val combined = CompletableFuture.allOf(future1, future2, future3, future4);
        combined.thenAccept(ignored->this.refresh());
        return combined;
    }

    private void refresh(){
        cache.invalidateAll();
        this.maxX.get();
        this.maxZ.get();
        this.minX.get();
        this.minZ.get();
        this.length.get();
        this.weight.get();
    }

    @Override
    public int getWeight() {
        return weight.get();
    }

    @Override
    public int getLength() {
        return length.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return manyWorld.unload().thenRun(()->{});
    }

    @Override
    public boolean validate() {
        return true;
    }


}
