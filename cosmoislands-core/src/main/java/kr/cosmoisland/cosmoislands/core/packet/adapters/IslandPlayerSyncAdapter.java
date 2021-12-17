package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class IslandPlayerSyncAdapter implements HelloAdapter<IslandPlayerSyncPacket> {

    final int maximumSize;

    @Override
    public String getIdentifier() {
        return IslandPlayerSyncPacket.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandPlayerSyncPacket packet) {
        buf.writeByte(packet.getType());
        buf.writeShort(packet.getList().size());
        for(UUID uuid : packet.getList()){
            ByteBufUtils.writeUUID(buf, uuid);
        }
    }

    @Override
    public IslandPlayerSyncPacket decode(ByteBuf buf) {
        byte syncType = buf.readByte();
        int size = buf.readShort();
        if(size > maximumSize)
            throw new IllegalArgumentException("read list was exceeded maximum size: "+maximumSize);
        List<UUID> list = new ArrayList<>();
        for(int i = 0; i < size; i++){
            UUID uuid = ByteBufUtils.readUUID(buf);
            list.add(uuid);
        }
        return new IslandPlayerSyncPacket(syncType, list);
    }
}
