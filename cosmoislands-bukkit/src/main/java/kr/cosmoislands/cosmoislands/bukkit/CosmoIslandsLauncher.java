package kr.cosmoislands.cosmoislands.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.manyworlds.bukkit.ManyWorlds;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.bukkit.world.MinecraftWorldHandlerFactory;
import kr.cosmoislands.cosmoislands.bukkit.world.MinecraftWorldHandlerInitializer;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoislands.api.ExternalRepository;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandConfiguration;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.bank.IslandInventory;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.api.level.IslandAchievements;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.points.IslandPoints;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.warp.IslandWarpsMap;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.bank.IslandInventoryModule;
import kr.cosmoislands.cosmoislands.bank.IslandVaultModule;
import kr.cosmoislands.cosmoislands.chat.IslandChatModule;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.Database;
import kr.cosmoislands.cosmoislands.level.IslandAchievementsModule;
import kr.cosmoislands.cosmoislands.level.IslandLevelModule;
import kr.cosmoislands.cosmoislands.member.PlayersMapModule;
import kr.cosmoislands.cosmoislands.points.IslandPointsModule;
import kr.cosmoislands.cosmoislands.protection.IslandPermissionsMapModule;
import kr.cosmoislands.cosmoislands.protection.IslandProtectionModule;
import kr.cosmoislands.cosmoislands.upgrade.IslandUpgradeModule;
import kr.cosmoislands.cosmoislands.warp.IslandWarpModule;
import kr.cosmoislands.cosmoislands.world.IslandWorldModule;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CosmoIslandsLauncher {

    private final CosmoIslands service;
    private final RedisClient client;
    private final MySQLDatabase msLibMySQLDatabase;
    private final Logger logger;

    public <T> void registerExternalDependency(Class<T> clazz, T instance){
        service.getExternalRepository().registerService(clazz, instance);
    }

    public void initializeModules(IslandConfiguration configuration){
        RedisAsyncCommands<String, String> async = client.connect().async();
        ExternalRepository repo = service.getExternalRepository();
        IslandRegistry islandRegistry = service.getRegistry();
        Database database = service.getDatabase();
        IslandPlayerRegistry playerRegistry = service.getPlayerRegistry();
        IslandCloud cloud = service.getCloud();

        Server minecraftServer = repo.getRegisteredService(Server.class);
        HelloEveryone networkModule = repo.getRegisteredService(HelloEveryone.class);
        HelloPlayers playersModule = repo.getRegisteredService(HelloPlayers.class);
        CosmoChat cosmoChat = repo.getRegisteredService(CosmoChat.class);
        CosmoChatPrivateChat privateChat = repo.getRegisteredService(CosmoChatPrivateChat.class);
        CosmoTeleport cosmoTeleport = repo.getRegisteredService(CosmoTeleport.class);
        ManyWorlds manyWorlds = repo.getRegisteredService(ManyWorlds.class);
        BukkitExecutor executor = repo.getRegisteredService(BukkitExecutor.class);

        IslandChatModule chatModule = new IslandChatModule(
                cosmoChat,
                privateChat,
                playerRegistry,
                msLibMySQLDatabase,
                async,
                logger);
        IslandLevelModule levelModule = new IslandLevelModule(database, logger);

        IslandAchievementsModule achievementsModule = new IslandAchievementsModule(database, logger);

        IslandSettingsModule settingsModule = new IslandSettingsModule(
                database,
                cloud,
                async,
                configuration.getDefaultSettings(),
                logger);

        IslandPlayersMapModule playersMapModule = new PlayersMapModule(
                islandRegistry,
                playerRegistry,
                settingsModule,
                database,
                async,
                logger);

        IslandPointsModule pointsModule = new IslandPointsModule(database, logger);

        IslandPermissionsMapModule permissionsModule = new IslandPermissionsMapModule(
                configuration.getDefaultPermissions(),
                logger);

        IslandProtectionModule protectionModule = new IslandProtectionModule(
                permissionsModule,
                playersMapModule,
                settingsModule,
                islandRegistry,
                playerRegistry,
                cloud,
                logger);

        IslandUpgradeModule upgradeModule = new IslandUpgradeModule(
                islandRegistry,
                database,
                logger);

        MinecraftWorldHandlerFactory builder = new MinecraftWorldHandlerFactory(minecraftServer, settingsModule);
        IslandWorldModule worldModule = new IslandWorldModule(
                manyWorlds,
                database,
                configuration.getManyWorldsProperties(),
                configuration.getDefaultWorldBorder(),
                service.getThreadFactory(),
                builder,
                logger);

        MinecraftWorldHandlerInitializer.init(playerRegistry, worldModule.getOperationRegistry(), cosmoTeleport, executor);

        IslandInventoryModule bankModule = new IslandInventoryModule(database, executor, logger);

        IslandVaultModule vaultModule = new IslandVaultModule(database, logger);

        IslandWarpModule warpModule = new IslandWarpModule(database, islandRegistry, playerRegistry, cosmoTeleport, settingsModule, logger);

        service.registerModule(IslandWorld.class, worldModule);
        service.registerModule(IslandSettingsMap.class, settingsModule);
        service.registerModule(IslandWarpsMap.class, warpModule);
        service.registerModule(IslandPlayersMap.class, playersMapModule);
        service.registerModule(IslandInventory.class, bankModule);

        service.registerModule(IslandVault.class, vaultModule);
        service.registerModule(IslandChat.class, chatModule);
        service.registerModule(IslandLevel.class, levelModule);
        service.registerModule(IslandAchievements.class, achievementsModule);
        service.registerModule(IslandPoints.class, pointsModule);

        service.registerModule(IslandPermissionsMap.class, permissionsModule);
        service.registerModule(IslandProtection.class, protectionModule);
        service.registerModule(IslandUpgrade.class, upgradeModule);

        service.getRegistry().registerComponentId(IslandWorld.class, IslandWorld.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandSettingsMap.class, IslandSettingsMap.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPlayersMap.class, IslandPlayersMap.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandInventory.class, IslandInventory.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandVault.class, IslandVault.COMPONENT_ID);

        service.getRegistry().registerComponentId(IslandChat.class, IslandChat.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandLevel.class, IslandLevel.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandAchievements.class, IslandAchievements.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPoints.class, IslandPoints.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPermissionsMap.class, IslandPermissionsMap.COMPONENT_ID);

        service.getRegistry().registerComponentId(IslandProtection.class, IslandProtection.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandUpgrade.class, IslandUpgrade.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandWarpsMap.class, IslandWarpsMap.COMPONENT_ID);
    }

    public void launch() throws ExecutionException, InterruptedException {
        service.init();
    }
}
