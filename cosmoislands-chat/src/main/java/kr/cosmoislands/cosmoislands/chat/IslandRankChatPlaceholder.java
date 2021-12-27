package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmochat.core.api.ChatChannel;
import kr.cosmoislands.cosmochat.core.api.ChatComponent;
import kr.cosmoislands.cosmochat.core.api.ChatFormatPlaceholder;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.SneakyThrows;

public class IslandRankChatPlaceholder extends ChatFormatPlaceholder {

    private final IslandRegistry registry;
    private final IslandPlayerRegistry playerRegistry;

    public IslandRankChatPlaceholder(IslandRegistry registry, IslandPlayerRegistry playerRegistry) {
        super("%island_rank%");
        this.registry = registry;
        this.playerRegistry = playerRegistry;
    }

    @SneakyThrows
    @Override
    public String format(ChatChannel channel, ChatComponent component, String message) {
        if(message.contains(identifier)) {
            String rankPrefix = "";
            IslandPlayer player = playerRegistry.get(component.getSender());
            if (player != null) {
                try {
                    Island island = registry.getIsland(player.getIslandId().get()).get();
                    if (island != null) {
                        IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
                        MemberRank rank = map.getRank(player).get();
                        switch (rank) {
                            case OWNER:
                                rankPrefix = "[섬장]";
                                break;
                            case MEMBER:
                                rankPrefix = "[섬원]";
                                break;
                            case INTERN:
                                rankPrefix = "[알바]";
                                break;
                        }
                    }
                }catch (Throwable e){
                    DebugLogger.error(e);
                }
                return message.replace(identifier, rankPrefix);
            }
        }
        return message;
    }
}
