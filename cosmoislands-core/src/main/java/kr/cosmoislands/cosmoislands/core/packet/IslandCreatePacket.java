package kr.cosmoislands.cosmoislands.core.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class IslandCreatePacket extends IslandPacket{

    final String destination;
    final UUID uuid;

    @Getter
    @RequiredArgsConstructor
    public static class CreateExecuted extends IslandPacket{
        final String executor;
        final UUID uuid;
        final int id;
        final boolean success;

    }
}
