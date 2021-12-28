package kr.cosmoislands.cosmoislands.world.hellobungee;

import com.minepalm.hellobungee.api.HelloAdapter;
import io.netty.buffer.ByteBuf;

public class WorldOperationAdapter implements HelloAdapter<WorldOperationPacket> {
    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public void encode(ByteBuf byteBuf, WorldOperationPacket packet) {

    }

    @Override
    public WorldOperationPacket decode(ByteBuf byteBuf) {
        return null;
    }
}
