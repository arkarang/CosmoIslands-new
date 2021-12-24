package kr.cosmoislands.cosmoislands.chat;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmochat.privatechat.PrivateChat;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandChatModule implements IslandModule<IslandChat> {

    private final CosmoChat chatService;
    private final IslandPlayerRegistry playerRegistry;
    private final CosmoChatPrivateChat privateChatService;
    @Getter
    private final IslandChatDataModel model;
    @Getter
    private final Logger logger;

    LoadingCache<Integer, CompletableFuture<IslandChat>> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, CompletableFuture<IslandChat>>() {
        @Override
        public CompletableFuture<IslandChat> load(Integer integer) throws Exception {
            val future = privateChatService.getRegistry(IslandChatType.TOKEN).get(integer);
            return future.thenApply(chat->{
                return new CosmoIslandChat(integer, chatService, chat);
            });
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
        this.model.init();
        this.privateChatService.createCustomChatRegistry(IslandChatType.TOKEN, this.model, async);
        this.logger = logger;
    }

    @Override
    @SneakyThrows
    public CompletableFuture<IslandChat> getAsync(int islandId) {
        return cache.get(islandId);
    }

    @Override
    @SneakyThrows
    public IslandChat get(int islandId) {
        return getAsync(islandId).get();
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        return model.create(islandId, uuid);
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
