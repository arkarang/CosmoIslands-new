package kr.cosmoislands.cosmoislands.bukkit.level;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import kr.cosmoisland.cosmoislands.api.level.IslandLevel;
import kr.cosmoisland.cosmoislands.api.level.IslandRewardsRegistry;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.level.IslandAchievementsModule;
import kr.cosmoisland.cosmoislands.level.IslandLevelModule;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.AbstractRankingGUI;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.entity.Player;


public class LevelCommands {

    public static void init(IslandService service, PaperCommandManager manager, BukkitExecutor executor){
        IslandAchievementsModule achievementsModule = (IslandAchievementsModule)service.getModule(IslandAchievements.class);
        IslandLevelModule levelModule = (IslandLevelModule) service.getModule(IslandLevel.class);
        IslandRewardsRegistry registry = achievementsModule.getRewardDataRegistry();
        manager.registerCommand(new User(executor, levelModule, registry));
        manager.registerCommand(new Admin(executor, registry));
    }

    @RequiredArgsConstructor
    @CommandAlias("섬")
    protected static class User extends BaseCommand {

        private final BukkitExecutor executor;
        private final IslandLevelModule module;
        private final IslandRewardsRegistry registry;

        @Subcommand("레벨")
        public void info(Player player){
            PlayerPreconditions.of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null) {
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }
                        IslandLevel level = island.getComponent(IslandLevel.class);
                        level.getLevel().thenAccept(value -> {
                            player.sendMessage("섬 레벨: "+value);
                        });
                    });
        }

        @Subcommand("레벨 보상")
        public void reward(Player player){
            PlayerPreconditions.of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null) {
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }
                        val listFuture = registry.getAll();
                        IslandAchievements achievements = island.getComponent(IslandAchievements.class);
                        val mapFuture = achievements.asMap();
                        mapFuture.thenCombine(listFuture, (map, list)->{
                            AchievementGUI gui = new AchievementGUI(map, list);
                            executor.sync(()-> gui.openGUI(player));
                            return null;
                        });
                    });
        }

        @Subcommand("레벨 랭킹")
        public void ranking(Player player){
            player.sendMessage("랭킹 로드중...");

            module.getRanking().getTopOf(3).thenAccept(list ->{
                final AbstractRankingGUI gui = new AbstractRankingGUI(new AbstractRankingGUI.RankingComponents() {
                    @Override
                    public String getTitle() {
                        return "섬 랭킹";
                    }

                    @Override
                    public String formatData(int value) {
                        return "§a§l섬 §b§l레벨 §f§l: "+value+" §f§l레벨";
                    }
                }, list);
                executor.sync(()->gui.openGUI(player));
            });
        }
    }

    @Subcommand("섬관리")
    @RequiredArgsConstructor
    protected static class Admin extends BaseCommand{

        private final BukkitExecutor executor;
        private final IslandRewardsRegistry registry;
        //todo: implements this.
    }
}
