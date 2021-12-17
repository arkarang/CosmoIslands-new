package kr.cosmoisland.cosmoislands.core.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IslandDeletePacket extends IslandPacket {

    final String destination;
    final int id;

    @Getter
    @RequiredArgsConstructor
    public static class DeleteExecuted extends IslandPacket{
        final String executor;
        final int id;
        final boolean success;

    }
}
