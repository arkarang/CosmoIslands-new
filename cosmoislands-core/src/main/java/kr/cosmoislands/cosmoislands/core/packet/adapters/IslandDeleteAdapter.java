package kr.cosmoislands.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoislands.cosmoislands.core.packet.IslandDeletePacket;

public class IslandDeleteAdapter implements HelloAdapter<IslandDeletePacket> {

    @Override
    public String getIdentifier() {
        return IslandDeletePacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandDeletePacket packet) {
        ByteBufUtils.writeString(buf, packet.getDestination());
        buf.writeInt(packet.getId());
    }

    @Override
    public IslandDeletePacket decode(ByteBuf buf) {
        String destination = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        return new IslandDeletePacket(destination, id);
    }
    
}
