package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class IslandTransferAdapter implements HelloAdapter<IslandTransferPacket> {

    @Override
    public String getIdentifier() {
        return IslandTransferPacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandTransferPacket packet) {
        buf.writeInt(packet.getIslandID());
        ByteBufUtils.writeString(buf, packet.getOldOwner().toString());
        ByteBufUtils.writeString(buf, packet.getNewOwner().toString());
    }

    @Override
    public IslandTransferPacket decode(ByteBuf buf) {
        int id = buf.readInt();
        String old = ByteBufUtils.readString(buf);
        String newOwner = ByteBufUtils.readString(buf);
        return new IslandTransferPacket(id, UUID.fromString(old), UUID.fromString(newOwner));
    }
}
