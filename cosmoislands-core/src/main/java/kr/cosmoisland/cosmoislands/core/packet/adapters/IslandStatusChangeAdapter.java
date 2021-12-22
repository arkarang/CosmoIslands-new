package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandUpdatePacket;

public class IslandStatusChangeAdapter implements HelloAdapter<IslandUpdatePacket> {
    @Override
    public String getIdentifier() {
        return IslandUpdatePacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandUpdatePacket packet) {
        ByteBufUtils.writeString(buf, packet.getDestination());
        buf.writeInt(packet.getIslandID());
        buf.writeBoolean(packet.isLoad());
    }

    @Override
    public IslandUpdatePacket decode(ByteBuf buf) {
        String destination = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        boolean b = buf.readBoolean();
        return new IslandUpdatePacket(destination, id, b);
    }
}
