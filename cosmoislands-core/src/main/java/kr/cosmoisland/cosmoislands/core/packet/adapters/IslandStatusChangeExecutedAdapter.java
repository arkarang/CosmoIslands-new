package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandUpdatePacket;

public class IslandStatusChangeExecutedAdapter implements HelloAdapter<IslandUpdatePacket.UpdateExecuted> {
    
    @Override
    public String getIdentifier() {
        return IslandUpdatePacket.UpdateExecuted.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandUpdatePacket.UpdateExecuted executed) {
        ByteBufUtils.writeString(buf, executed.getExecutor());
        buf.writeInt(executed.getId());
        buf.writeBoolean(executed.isLoad());
        buf.writeBoolean(executed.isSuccess());
    }

    @Override
    public IslandUpdatePacket.UpdateExecuted decode(ByteBuf buf) {
        String executor = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        boolean loadType = buf.readBoolean();
        boolean success = buf.readBoolean();
        return new IslandUpdatePacket.UpdateExecuted(executor, id, loadType, success);
    }
}
