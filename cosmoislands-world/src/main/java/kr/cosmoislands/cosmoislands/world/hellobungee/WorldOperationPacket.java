package kr.cosmoislands.cosmoislands.world.hellobungee;

import lombok.Data;

@Data
public class WorldOperationPacket {

    final int islandId;
    final String type;
    final String operationKey;
}
