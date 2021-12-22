package kr.cosmoislands.cosmoislands.bungee;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bungee.HelloBungee;
import com.minepalm.helloplayer.core.HelloPlayers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandServer;
import kr.cosmoisland.cosmoislands.api.ServerRegistration;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.chat.IslandChatModule;
import kr.cosmoisland.cosmoislands.core.CosmoIslandCloud;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmochat.bungee.CosmoChatBungee;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.bungee.CosmoTeleportBungee;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CosmoIslandsBungee extends Plugin {

    @Getter
    static CosmoIslands cosmoIslands;

    @Override
    public void onEnable() {
        HelloEveryone networkModule = HelloBungee.getInst().getMain();
        HelloPlayers playersModule = HelloPlayers.getInst();
        MySQLDatabase msLibMySQLDatabase = CosmoDataSource.mysql("island");
        RedisClient redis = CosmoDataSource.redis("island");
        RedisAsyncCommands<String, String> async = redis.connect().async();
        CosmoChat cosmoChat = CosmoChatBungee.getService();
        CosmoChatPrivateChat privateChatAddon = CosmoChatBungee.getPrivateChatAddon();
        CosmoTeleport cosmoTeleport = CosmoTeleportBungee.getService();

        Conf conf = new Conf(this);

        try {
            cosmoIslands = new CosmoIslands(networkModule, redis, msLibMySQLDatabase, this.getLogger());
            IslandChatModule chatModule = new IslandChatModule(cosmoChat, privateChatAddon, cosmoIslands.getPlayerRegistry(), msLibMySQLDatabase, redis.connect().async(), this.getLogger());
            cosmoIslands.registerModule(IslandChat.class, chatModule);
            cosmoIslands.init();
            ProxyServer.getInstance().getPluginManager().registerListener(this, new Listener(cosmoIslands, conf.getMaxIslands()));
            if(conf.updateServerRegistration()) {
                for (Map.Entry<IslandServer.Type, List<String>> entry : conf.getServerList().entrySet()) {
                    ServerRegistration registration = cosmoIslands.getCloud().getServerRegistration();
                    entry.getValue().forEach(name -> registration.registerServer(name, entry.getKey()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }

}
