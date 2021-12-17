package kr.cosmoisland.cosmoislands.core.utils;

public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;
}
