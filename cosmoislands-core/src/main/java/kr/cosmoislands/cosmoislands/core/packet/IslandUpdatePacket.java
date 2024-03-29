package kr.cosmoislands.cosmoislands.core.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IslandUpdatePacket extends IslandPacket {

    final String destination;
    final int islandID;
    final boolean load;

    @Getter
    @RequiredArgsConstructor
    public static class UpdateExecuted extends IslandPacket{
        final String executor;
        final int id;
        final boolean load;
        final boolean success;

    }
}
