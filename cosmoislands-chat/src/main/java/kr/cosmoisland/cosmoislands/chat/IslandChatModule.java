package kr.cosmoisland.cosmoislands.chat;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMapModule;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandChatModule implements IslandModule<IslandChat> {

    private final CosmoChat chatService;
    private final IslandPlayerRegistry playerRegistry;
    private final CosmoChatPrivateChat privateChatService;
    private final IslandChatDataModel model;
    @Getter
    private final Logger logger;

    LoadingCache<Integer, IslandChat> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandChat>() {
        @Override
        public IslandChat load(Integer integer) throws Exception {
            return new CosmoIslandChat(integer, chatService, null);
        }
    });

    public IslandChatModule(CosmoChat cosmoChat,
                            CosmoChatPrivateChat privateChatService,
                            IslandPlayerRegistry playerRegistry,
                            MySQLDatabase database,
                            RedisAsyncCommands<String, String> async,
                            Logger logger){
        this.chatService = cosmoChat;
        this.privateChatService = privateChatService;
        this.playerRegistry = playerRegistry;
        this.model = new IslandChatDataModel(database, "cosmoislands_chat_users", "cosmoislands_chat_list", "cosmoislands_islands");
        this.privateChatService.createCustomChatRegistry(IslandChatType.TOKEN, this.model, async);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandChat> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandChat get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        IslandRankChatPlaceholder placeholder = new IslandRankChatPlaceholder(service.getRegistry(), playerRegistry);
        this.chatService.getFormatRegistry().registerPlaceholder(placeholder.getIdentifier(), placeholder);
        IslandPlayersMapModule playersMapModule = (IslandPlayersMapModule) service.getModule(IslandPlayersMap.class);
        playersMapModule.getStrategyRegistry().addStrategy("chat", new PlayerChatModificationStrategy(this));
        service.getFactory().addLast("chat", new IslandChatLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
