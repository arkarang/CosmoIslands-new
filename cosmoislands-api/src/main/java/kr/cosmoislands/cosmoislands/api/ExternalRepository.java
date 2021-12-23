package kr.cosmoislands.cosmoislands.api;

public interface ExternalRepository {

    <T> T getRegisteredService(Class<T> clazz);

    <T> void registerService(Class<T> clazz, T instance);
}
