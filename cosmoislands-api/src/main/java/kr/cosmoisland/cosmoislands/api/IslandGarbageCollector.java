package kr.cosmoisland.cosmoislands.api;

public interface IslandGarbageCollector {

    long getInvalidateLifetime();

    boolean validate(Island island);

    void invalidate(Island island);


}
