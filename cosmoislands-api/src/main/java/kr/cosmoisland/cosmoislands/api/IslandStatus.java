package kr.cosmoisland.cosmoislands.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IslandStatus {

    OFFLINE(0), ONLINE(1), LOADING(2), UNLOADING(3), ERROR(99);

    private final int code;

    public byte code(){
        return (byte)code;
    }
}
