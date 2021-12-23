package kr.cosmoislands.cosmoislands.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IslandStatus {

    OFFLINE(0), ONLINE(1), LOADING(2), UNLOADING(3), ERROR(99);

    private final int code;

    public byte code(){
        return (byte)code;
    }

    public static IslandStatus byCode(byte b){
        switch (b){
            case 0:
                return OFFLINE;
            case 1:
                return ONLINE;
            case 2:
                return LOADING;
            case 3:
                return UNLOADING;
            default:
                return ERROR;
        }
    }
}
