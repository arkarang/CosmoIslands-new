package kr.cosmoislands.cosmoislands.bungee;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bungee.HelloBungee;
import com.minepalm.helloplayer.core.HelloPlayers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmochat.bungee.CosmoChatBungee;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.RedisChannelFactory;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoislands.api.IslandServer;
import kr.cosmoislands.cosmoislands.api.ServerRegistration;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.bungee.config.BungeeYamlIslandConfiguration;
import kr.cosmoislands.cosmoislands.bungee.config.MySQLBungeeIslandConfiguration;
import kr.cosmoislands.cosmoislands.chat.IslandChatModule;
import kr.cosmoislands.cosmoislands.chat.IslandChatType;
import kr.cosmoislands.cosmoislands.chat.IslandRankChatPlaceholder;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.core.HelloBungeeInitializer;
import kr.cosmoislands.cosmoislands.core.config.MySQLPropertyDataModel;
import kr.cosmoislands.cosmoislands.member.PlayersMapModule;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmoislands.upgrade.IslandUpgradeModule;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.bungee.CosmoTeleportBungee;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoIslandsBungee extends Plugin {

    @Getter
    static CosmoIslands cosmoIslands;

    @Override
    public void onEnable() {
        DebugLogger.setLogger(this.getLogger());
        DebugLogger.setEnableDebug(true);

        Conf conf = new Conf(this);
        BungeeYamlIslandConfiguration yamlConfig = new BungeeYamlIslandConfiguration(this);

        HelloEveryone networkModule = HelloBungee.getInst().getMain();
        MySQLDatabase msLibMySQLDatabase = CosmoDataSource.mysql(conf.getMySQLName());
        RedisClient redis = CosmoDataSource.redis(conf.getRedisName());
        CosmoChat cosmoChat = CosmoChatBungee.getService();
        CosmoChatPrivateChat privateChatAddon = CosmoChatBungee.getPrivateChatAddon();
        RedisAsyncCommands<String, String> async = redis.connect().async();

        MySQLPropertyDataModel model = new MySQLPropertyDataModel("cosmoislands_properties", msLibMySQLDatabase);
        model.init();

        MySQLBungeeIslandConfiguration mysqlSettings = new MySQLBungeeIslandConfiguration(model);

        try {
            if(yamlConfig.doMySQLSync()){
                mysqlSettings.migrate(yamlConfig).get();
            }

            cosmoIslands = new CosmoIslands(networkModule, redis, msLibMySQLDatabase, this.getLogger());

            if(conf.updateServerRegistration()) {
                for (Map.Entry<IslandServer.Type, List<String>> entry : conf.getServerList().entrySet()) {
                    ServerRegistration registration = cosmoIslands.getCloud().getServerRegistration();
                    for (String name : entry.getValue()) {
                        registration.registerServer(name, entry.getKey()).get();
                    }
                }
            }

            IslandSettingsModule settingsModule = new IslandSettingsModule(
                    cosmoIslands.getDatabase(),
                    cosmoIslands.getCloud(),
                    async,
                    mysqlSettings.getDefaultSettings(),
                    this.getLogger());

            IslandChatModule chatModule = new IslandChatModule(
                    cosmoChat,
                    privateChatAddon,
                    cosmoIslands.getPlayerRegistry(),
                    msLibMySQLDatabase,
                    async,
                    this.getLogger());

            IslandPlayersMapModule playersMapModule = new PlayersMapModule(
                    cosmoIslands.getRegistry(),
                    cosmoIslands.getPlayerRegistry(),
                    settingsModule,
                    cosmoIslands.getDatabase(),
                    async,
                    this.getLogger());

            IslandUpgradeModule upgradeModule = new IslandUpgradeModule(
                    cosmoIslands.getRegistry(),
                    cosmoIslands.getDatabase(),
                    this.getLogger());

            cosmoIslands.registerModule(IslandSettingsMap.class, settingsModule);
            cosmoIslands.registerModule(IslandChat.class, chatModule);
            cosmoIslands.registerModule(IslandPlayersMap.class, playersMapModule);
            cosmoIslands.registerModule(IslandUpgrade.class, upgradeModule);

            cosmoIslands.init();

            List<CompletableFuture<?>> futures = new ArrayList<>();

            //Yaml 파일에서 디비 테이블에 동기화된 정보를 UpgradeSettings 테이블에 갱신할 지 여부를 체크합니다.
            if(yamlConfig.getUpdateUpgradeSettings()){
                for (IslandUpgradeSettings setting : mysqlSettings.getDefaultUpgradeSettings().values()) {
                    val future = upgradeModule.getSettingsRegistry().setSetting(setting);
                    futures.add(future);
                }
            }
            DebugLogger.handle("update upgarde settings ", CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])));

            IslandRankChatPlaceholder placeholder = new IslandRankChatPlaceholder(cosmoIslands.getRegistry(), cosmoIslands.getPlayerRegistry());
            cosmoChat.getFormatRegistry().registerPlaceholder("island_rank", placeholder);
            cosmoChat.getChannelRegistry().register(IslandChatType.TOKEN, new RedisChannelFactory());

            ProxyServer.getInstance().getPluginManager().registerListener(this, new Listener(cosmoIslands, cosmoChat, conf.getMaxIslands()));
            HelloBungeeInitializer.init(networkModule, cosmoIslands);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }



}
