package kr.cosmoislands.cosmoislands.core.utils;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {

    void accept(T t, U u) throws Exception;
}
