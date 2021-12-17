package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;

import java.util.UUID;

public class IslandCreateExecutedAdapter implements HelloAdapter<IslandCreatePacket.CreateExecuted> {

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.CreateExecuted.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandCreatePacket.CreateExecuted executed) {
        ByteBufUtils.writeString(buf, executed.getExecutor());
        ByteBufUtils.writeUUID(buf, executed.getUuid());
        buf.writeInt(executed.getId());
        buf.writeBoolean(executed.isSuccess());
    }

    @Override
    public IslandCreatePacket.CreateExecuted decode(ByteBuf buf) {
        String executor = ByteBufUtils.readString(buf);
        UUID uuid = ByteBufUtils.readUUID(buf);
        int island_id = buf.readInt();
        boolean success = buf.readBoolean();
        return new IslandCreatePacket.CreateExecuted(executor, uuid, island_id, success);
    }
}
