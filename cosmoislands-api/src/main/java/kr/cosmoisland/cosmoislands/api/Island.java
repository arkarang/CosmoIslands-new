package kr.cosmoisland.cosmoislands.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 섬 인터페이스
 */
public interface Island extends IslandComponent{

    int NIL_ID = -1;

    int getId();

    <T extends IslandComponent> T getComponent(Class<T> clazz);

    Map<Class<? extends IslandComponent>, ? extends IslandComponent> getComponents();

    CompletableFuture<Boolean> isLoaded();

    CompletableFuture<IslandServer> getLocated();

    @Override
    @SuppressWarnings("unchecked")
    CompletableFuture<Island> sync();

    <T extends IslandComponent> CompletableFuture<T> sync(Class<T> clazz);

    CompletableFuture<Long> getUptime();


}
