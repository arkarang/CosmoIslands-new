package kr.cosmoislands.cosmoislands.core.utils;

public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;
}
