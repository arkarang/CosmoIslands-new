package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;

public class IslandStatusChangeAdapter implements HelloAdapter<IslandStatusChangePacket> {
    @Override
    public String getIdentifier() {
        return IslandStatusChangePacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandStatusChangePacket packet) {
        ByteBufUtils.writeString(buf, packet.getDestination());
        buf.writeInt(packet.getIslandID());
        buf.writeBoolean(packet.isLoad());
    }

    @Override
    public IslandStatusChangePacket decode(ByteBuf buf) {
        String destination = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        boolean b = buf.readBoolean();
        return new IslandStatusChangePacket(destination, id, b);
    }
}
