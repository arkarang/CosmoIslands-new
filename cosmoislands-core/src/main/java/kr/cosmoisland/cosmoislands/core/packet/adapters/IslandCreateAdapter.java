package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;

import java.util.UUID;

public class IslandCreateAdapter implements HelloAdapter<IslandCreatePacket> {

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandCreatePacket packet) {
        //todo: remove after testing.
        System.out.println("encoding: "+getIdentifier()+" destination: "+packet.getDestination()+" uuid: "+packet.getUuid());
        ByteBufUtils.writeString(buf, packet.getDestination());
        ByteBufUtils.writeUUID(buf, packet.getUuid());
    }

    @Override
    public IslandCreatePacket decode(ByteBuf buf) {
        String destination = ByteBufUtils.readString(buf);
        UUID uuid = ByteBufUtils.readUUID(buf);
        //todo: remove after testing.
        System.out.println("decoding: "+getIdentifier()+" destination: "+destination+" uuid: "+uuid);
        return new IslandCreatePacket(destination, uuid);
    }
}
