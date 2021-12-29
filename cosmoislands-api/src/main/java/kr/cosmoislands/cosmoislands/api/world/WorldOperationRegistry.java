package kr.cosmoislands.cosmoislands.api.world;

import java.util.function.Consumer;

public interface WorldOperationRegistry {

    <T extends IslandWorldHandler> void registerType(Class<T> clazz);

    <T extends IslandWorldHandler> void registerOperation(Class<T> clazz, String name, WorldOperation<T> operation);

    <T extends IslandWorldHandler> WorldOperation<T> getOperation(Class<T> clazz, String key);

}
