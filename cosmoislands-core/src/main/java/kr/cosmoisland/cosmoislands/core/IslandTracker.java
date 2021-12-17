package kr.cosmoisland.cosmoislands.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandTracker {

    public static final byte OFFLINE = 0;
    public static final byte ONLINE = 1;
    public static final byte LOADING = 2;
    public static final byte UNLOADING = 3;
    public static final byte SOMETHING_WRONG = 99;

    @Getter
    final int id;
    final IslandTrackerLoader loader;

    public CompletableFuture<Optional<String>> getLocatedServer(){
        return loader.getLoadedServer(id);
    }

    public CompletableFuture<Boolean> isLoaded(){
        return loader.statusEquals(id, IslandTracker.ONLINE);
    }

    public CompletableFuture<Boolean> statusEquals(byte b){
        return loader.statusEquals(id, b);
    }

    public CompletableFuture<Byte> getStatus(){
        return loader.getStatus(id);
    }
}
