package kr.cosmoislands.cosmoislands.bukkit.points;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.points.IslandPoints;
import kr.cosmoislands.cosmoislands.api.points.IslandVoter;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.AbstractRankingGUI;
import kr.cosmoislands.cosmoislands.points.IslandPointsModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

public class PointsCommands {

    public static void init(PaperCommandManager manager, IslandService service, HelloPlayers players, BukkitExecutor executor){
        IslandPointsModule pointsModule = (IslandPointsModule) service.getModule(IslandPoints.class);
        AbstractRankingGUI.Factory factory = new AbstractRankingGUI.Factory(players, new AbstractRankingGUI.RankingComponents() {
            @Override
            public String getTitle() {
                return "섬 랭킹";
            }

            @Override
            public String formatData(int value) {
                return "§a§l섬 §b§l인기도 §f§l: "+value+"§f§l포인트";
            }
        }, service.getRegistry(), island -> {
            IslandPoints points = island.getComponent(IslandPoints.class);
            return points.getPoints();
        });

        manager.registerCommand(new User(factory, pointsModule, executor));
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand {

        private final AbstractRankingGUI.Factory factory;
        private final IslandPointsModule module;
        private final BukkitExecutor executor;

        @Subcommand("인기도")
        public void help(Player player){
            player.sendMessage("/섬 인기도 랭킹 - 섬 인기도 랭킹을 봅니다.");
            player.sendMessage("/섬 인기도 올리기 - 지금 서 있는 섬의 인기도를 올립니다.");
            player.sendMessage("/섬 인기도 내리기 - 지금 서 있는 섬의 인기도를 내립니다.");
        }

        @Subcommand("인기도 랭킹")
        public void ranking(Player player){
            player.sendMessage("랭킹 로드 중...");
            module.getRanking().getTopOf(3).thenAccept(list->{
                factory.build(list).thenAccept(gui->executor.sync(()->gui.openGUI(player)));
            });

        }

        @Subcommand("인기도 올리기")
        public void up(Player player){
            vote(player, 1);
        }

        @Subcommand("인기도 내리기")
        public void down(Player player){
            vote(player, -1);
        }

        private void vote(Player player, int value){
            try{
                IslandPreconditions preconditions = IslandPreconditions.of(player.getWorld());
                Island island = preconditions.getIsland();
                IslandPoints points = island.getComponent(IslandPoints.class);
                IslandVoter voter = module.getVoter(player.getUniqueId());

                voter.canVote().thenAccept(canVote->{
                    if(canVote){
                        voter.vote(island.getId(), points, value);
                        player.sendMessage("지금 서 있는 섬에 추천했습니다!");
                    }else{
                        player.sendMessage("하루에 한번만 추천 가능합니다.");
                    }
                });
            }catch (IllegalArgumentException e){
                player.sendMessage("해당 명령어는 섬 안에서만 입력할수 있습니다.");
            }
        }

    }

    protected static class Admin extends BaseCommand {

    }
}
