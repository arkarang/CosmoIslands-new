package kr.cosmoislands.cosmoislands.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 섬 인터페이스
 */
public interface Island extends IslandComponent{

    byte COMPONENT_ID = 0;

    int NIL_ID = -1;

    int getId();

    boolean isLocal();

    <T extends IslandComponent> T getComponent(Class<T> clazz);

    Map<Class<? extends IslandComponent>, ? extends IslandComponent> getComponents();

    CompletableFuture<IslandStatus> getStatus();

    CompletableFuture<IslandServer> getLocated();

    @Override
    @SuppressWarnings("unchecked")
    CompletableFuture<Island> sync();

    <T extends IslandComponent> CompletableFuture<T> sync(Class<T> clazz);

    CompletableFuture<Long> getUptime();


}
