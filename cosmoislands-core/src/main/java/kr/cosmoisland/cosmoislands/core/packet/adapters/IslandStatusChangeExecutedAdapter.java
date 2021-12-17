package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;

public class IslandStatusChangeExecutedAdapter implements HelloAdapter<IslandStatusChangePacket.UpdateExecuted> {
    
    @Override
    public String getIdentifier() {
        return IslandStatusChangePacket.UpdateExecuted.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandStatusChangePacket.UpdateExecuted executed) {
        ByteBufUtils.writeString(buf, executed.getExecutor());
        buf.writeInt(executed.getId());
        buf.writeBoolean(executed.isLoad());
        buf.writeBoolean(executed.isSuccess());
    }

    @Override
    public IslandStatusChangePacket.UpdateExecuted decode(ByteBuf buf) {
        String executor = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        boolean loadType = buf.readBoolean();
        boolean success = buf.readBoolean();
        return new IslandStatusChangePacket.UpdateExecuted(executor, id, loadType, success);
    }
}
