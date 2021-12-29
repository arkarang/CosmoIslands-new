package kr.cosmoislands.cosmoislands.bukkit;

import co.aikar.commands.PaperCommandManager;
import com.minepalm.arkarangutils.bukkit.ArkarangGUIListener;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.invitation.ArkarangInvitation;
import com.minepalm.arkarangutils.invitation.InvitationService;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bukkit.HelloBukkit;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.manyworlds.bukkit.ManyWorlds;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmochat.bukkit.CosmoChatBukkit;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.helper.CosmoChatHelper;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoislands.api.ExternalRepository;
import kr.cosmoislands.cosmoislands.api.IslandConfiguration;
import kr.cosmoislands.cosmoislands.api.warp.IslandWarpsMap;
import kr.cosmoislands.cosmoislands.bukkit.bank.BankCommands;
import kr.cosmoislands.cosmoislands.bukkit.chat.ChatCommands;
import kr.cosmoislands.cosmoislands.bukkit.config.BukkitConfiguration;
import kr.cosmoislands.cosmoislands.bukkit.config.MySQLBukkitIslandConfiguration;
import kr.cosmoislands.cosmoislands.bukkit.level.LevelCommands;
import kr.cosmoislands.cosmoislands.bukkit.member.IslandInternInvitationStrategy;
import kr.cosmoislands.cosmoislands.bukkit.member.IslandInvitationStrategy;
import kr.cosmoislands.cosmoislands.bukkit.member.MemberCommands;
import kr.cosmoislands.cosmoislands.bukkit.points.PointsCommands;
import kr.cosmoislands.cosmoislands.bukkit.protection.CacheUpdateListener;
import kr.cosmoislands.cosmoislands.bukkit.protection.ProtectionCommands;
import kr.cosmoislands.cosmoislands.bukkit.protection.ProtectionListener;
import kr.cosmoislands.cosmoislands.bukkit.settings.SettingsCommands;
import kr.cosmoislands.cosmoislands.bukkit.upgrade.UpgradeCommands;
import kr.cosmoislands.cosmoislands.bukkit.utils.IslandIcon;
import kr.cosmoislands.cosmoislands.bukkit.warp.IslandWarpCommands;
import kr.cosmoislands.cosmoislands.bukkit.world.MinecraftWorldHandlerFactory;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.core.HelloBungeeInitializer;
import kr.cosmoislands.cosmoislands.core.config.AbstractMySQLIslandConfiguration;
import kr.cosmoislands.cosmoislands.core.config.MySQLPropertyDataModel;
import kr.cosmoislands.cosmoislands.warp.IslandWarpModule;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.bukkit.CosmoTeleportBukkit;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CosmoIslandsBukkit extends JavaPlugin {

    @Getter
    static CosmoIslands cosmoIslands;

    @Override
    public void onEnable() {
        DebugLogger.setLogger(this.getLogger());
        DebugLogger.setEnableDebug(true);

        BukkitConfiguration config = new BukkitConfiguration(this);

        HelloEveryone networkModule = HelloBukkit.getInst().getMain();
        HelloPlayers playersModule = HelloPlayers.getInst();
        MySQLDatabase msLibMySQLDatabase = CosmoDataSource.mysql(config.getMySQLName());
        RedisClient redis = CosmoDataSource.redis(config.getRedisName());
        CosmoChat cosmoChat = CosmoChatBukkit.getService();
        CosmoChatPrivateChat privateChatAddon = CosmoChatBukkit.getPrivateChatAddon();
        CosmoTeleport cosmoTeleport = CosmoTeleportBukkit.getService();
        ManyWorlds manyWorlds = ManyWorlds.getInst();
        BukkitExecutor executor = new BukkitExecutor(this, Bukkit.getScheduler());

        MySQLPropertyDataModel propertyDataModel = new MySQLPropertyDataModel("cosmoislands_properties", msLibMySQLDatabase);
        propertyDataModel.init();
        AbstractMySQLIslandConfiguration islandConfig = new MySQLBukkitIslandConfiguration(propertyDataModel);

        Economy economy = null;
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(provider != null) {
            economy = provider.getProvider();
        }
        if(economy == null){
            getLogger().severe("Economy 플러그인을 찾을수 없습니다. 플러그인을 종료합니다.");
            return;
        }

        try {
            cosmoIslands = new CosmoIslands(networkModule, redis, msLibMySQLDatabase, this.getLogger());
            HelloBungeeInitializer.init(networkModule, cosmoIslands);
            CosmoIslandsLauncher launcher = new CosmoIslandsLauncher(cosmoIslands, redis, msLibMySQLDatabase, this.getLogger());

            launcher.registerExternalDependency(Server.class, Bukkit.getServer());
            launcher.registerExternalDependency(HelloEveryone.class, networkModule);
            launcher.registerExternalDependency(HelloPlayers.class, playersModule);
            launcher.registerExternalDependency(CosmoChat.class, cosmoChat);
            launcher.registerExternalDependency(CosmoChatPrivateChat.class, privateChatAddon);
            launcher.registerExternalDependency(CosmoTeleport.class, cosmoTeleport);
            launcher.registerExternalDependency(ManyWorlds.class, manyWorlds);
            launcher.registerExternalDependency(BukkitExecutor.class, executor);
            launcher.registerExternalDependency(Economy.class, economy);

            launcher.initializeModules(islandConfig);
            launcher.launch();

            PlayerPreconditions.getFactory().setPlayerRegistry(cosmoIslands.getPlayerRegistry());
            PlayerPreconditions.getFactory().setExecutor(Executors.newScheduledThreadPool(4, cosmoIslands.getThreadFactory()));

            IslandPreconditions.getFactory().setIslandRegistry(cosmoIslands.getRegistry());
            IslandPreconditions.getFactory().setPlayerRegistry(cosmoIslands.getPlayerRegistry());

            IslandIcon.setPlayerModule(playersModule);

            this.initializeCommands(cosmoIslands, islandConfig, redis);
            this.initializeListeners(cosmoIslands, executor);

            DebugLogger.setEnableDebug(config.isDebug());
            //todo: remove this after testing.
            DebugLogger.setEnableDebug(true);

        }catch (Exception e){
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void initializeCommands(CosmoIslands islands, IslandConfiguration configuration, RedisClient client){
        PaperCommandManager manager = new PaperCommandManager(this);
        ExternalRepository repo = islands.getExternalRepository();

        //todo: redis connection 정리하기
        StatefulRedisConnection<String, String> connection = client.connect();

        RedisAsyncCommands<String, String> timeoutAsync = connection.async();
        timeoutAsync.setTimeout(Duration.ofMillis(30000L));

        RedisAsyncCommands<String, String> async2 = connection.async();
        async2.setTimeout(Duration.ofMillis(1000L*60L*10L));

        HelloEveryone networkModule = repo.getRegisteredService(HelloEveryone.class);
        HelloPlayers playersModule = repo.getRegisteredService(HelloPlayers.class);
        CosmoChat cosmoChat = repo.getRegisteredService(CosmoChat.class);
        CosmoChatPrivateChat privateChat = repo.getRegisteredService(CosmoChatPrivateChat.class);
        CosmoTeleport cosmoTeleport = repo.getRegisteredService(CosmoTeleport.class);
        ManyWorlds manyWorlds = repo.getRegisteredService(ManyWorlds.class);
        CosmoChatHelper helper = new CosmoChatHelper(cosmoChat);
        BukkitExecutor executor = repo.getRegisteredService(BukkitExecutor.class);

        BankCommands.init(islands, manager);
        ChatCommands.init(islands, manager);
        LevelCommands.init(islands, manager, playersModule, executor, configuration.getLevelLorePattern(), configuration.getLevelLore());

        ExecutorService service = Executors.newScheduledThreadPool(4, islands.getThreadFactory());
        InvitationService memberInvitation, internInvitation;
        memberInvitation = ArkarangInvitation.redis("island_member", 30000, timeoutAsync,
                new IslandInvitationStrategy(islands.getRegistry(), islands.getPlayerRegistry(), playersModule, service, helper));
        internInvitation = ArkarangInvitation.redis("island_intern", 30000, timeoutAsync,
                new IslandInternInvitationStrategy(islands.getRegistry(), islands.getPlayerRegistry(), playersModule, service, helper));
        MemberCommands.init(manager, islands, playersModule, memberInvitation, internInvitation, helper, executor);

        PointsCommands.init(manager, islands, playersModule, executor);
        ProtectionCommands.init(manager);
        SettingsCommands.init(manager, executor);
        UpgradeCommands.init(manager, executor);
        IslandWarpCommands.init(manager, islands, (IslandWarpModule) islands.getModule(IslandWarpsMap.class), executor);
        GenericCommands.init(manager, islands, async2, executor);

        manager.registerCommand(new TestCommands(islands, helper, executor));
    }

    private void initializeListeners(CosmoIslands islands, BukkitExecutor executor){
        Bukkit.getPluginManager().registerEvents(new CacheUpdateListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(executor), this);
        ArkarangGUIListener.init();
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        cosmoIslands.shutdown();
    }
}
