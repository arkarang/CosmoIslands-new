package kr.cosmoislands.cosmoislands.core.packet;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class IslandIdPacket {

    final int islandId;

}
