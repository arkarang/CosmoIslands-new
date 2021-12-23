package kr.cosmoislands.cosmoislands.core;

import kr.cosmoislands.cosmoislands.api.ExternalRepository;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class CosmoExternalRepository implements ExternalRepository {

    private final ConcurrentHashMap<Class<?>, Object> registeredServices = new ConcurrentHashMap<>();

    CosmoExternalRepository(){}

    @Override
    public <T> T getRegisteredService(Class<T> clazz) {
        return (T)registeredServices.get(clazz);
    }

    @Override
    public <T> void registerService(Class<T> clazz, T instance) {
        registeredServices.put(clazz, instance);
    }
}
