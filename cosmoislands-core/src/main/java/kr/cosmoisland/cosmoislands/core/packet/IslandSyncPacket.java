package kr.cosmoisland.cosmoislands.core.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IslandSyncPacket extends IslandPacket{

    final int islandID;
    final byte componentID;

    @Getter
    @RequiredArgsConstructor
    public static class Executed extends IslandPacket{

        final int islandID;
        final byte componentID;
        final boolean success;

    }
}
