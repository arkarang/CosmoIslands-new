package kr.cosmoisland.cosmoislands.protection.hellobungee;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class ProtectionUpdatePacketAdapter implements HelloAdapter<ProtectionUpdatePacket> {

    @Override
    public String getIdentifier() {
        return ProtectionUpdatePacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, ProtectionUpdatePacket packet) {
        buf.writeInt(packet.getIslandId());
        ByteBufUtils.writeUUID(buf, packet.getUuid());
    }

    @Override
    public ProtectionUpdatePacket decode(ByteBuf buf) {
        int islandId = buf.readInt();
        UUID uuid = ByteBufUtils.readUUID(buf);
        return new ProtectionUpdatePacket(islandId, uuid);
    }
}
