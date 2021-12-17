package kr.cosmoisland.cosmoislands.protection.hellobungee;

import kr.cosmoisland.cosmoislands.core.packet.IslandPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
public class ProtectionUpdatePacket extends IslandPacket {

    int islandId;
    UUID uuid;

    public ProtectionUpdatePacket(int islandId, UUID uuid) {
        this.islandId = islandId;
        this.uuid = uuid;
    }
}
