package kr.cosmoisland.cosmoislands.core.packet.adapters;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import kr.cosmoisland.cosmoislands.core.packet.IslandDeletePacket;

public class IslandDeleteExecutedAdapter implements HelloAdapter<IslandDeletePacket.DeleteExecuted> {

    @Override
    public String getIdentifier() {
        return IslandDeletePacket.DeleteExecuted.class.getSimpleName();
    }

    @Override
    public void encode(ByteBuf buf, IslandDeletePacket.DeleteExecuted executed) {
        ByteBufUtils.writeString(buf, executed.getExecutor());
        buf.writeInt(executed.getId());
        buf.writeBoolean(executed.isSuccess());
    }

    @Override
    public IslandDeletePacket.DeleteExecuted decode(ByteBuf buf) {
        String executor = ByteBufUtils.readString(buf);
        int id = buf.readInt();
        boolean success = buf.readBoolean();
        return new IslandDeletePacket.DeleteExecuted(executor, id, success);
    }
}
