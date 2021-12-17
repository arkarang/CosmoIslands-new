package kr.cosmoislands.cosmoislands.bukkit.points;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoislands.cosmoislands.bukkit.utils.AbstractRankingGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class PointsCommands {

    @CommandAlias("섬")
    protected static class User extends BaseCommand {

        @Subcommand("인기도")
        public void help(Player player){
            player.sendMessage("/섬 인기도 랭킹 - 섬 인기도 랭킹을 봅니다.");
            player.sendMessage("/섬 인기도 올리기 - 지금 서 있는 섬의 인기도를 올립니다.");
            player.sendMessage("/섬 인기도 내리기 - 지금 서 있는 섬의 인기도를 내립니다.");
        }

        @Subcommand("인기도 랭킹")
        public void ranking(Player player){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                player.sendMessage("랭킹 로드 중...");
                try {
                    final AbstractRankingGUI gui = new AbstractRankingGUI(new AbstractRankingGUI.RankingComponents() {
                        @Override
                        public String getTitle() {
                            return "섬 랭킹";
                        }

                        @Override
                        public String formatData(int value) {
                            return "§a§l섬 §b§l인기도 §f§l: "+value+"§f§l포인트";
                        }
                    }, db.getLoader(IslandDataLoader.class).getPointsRanking(3).get());
                    Bukkit.getScheduler().runTask(CosmoIslands.getInst(), ()->gui.openGUI(player));
                } catch (InterruptedException | ExecutionException e) {
                    player.sendMessage("랭킹 조회 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
                }
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
            int id = IslandUtils.getIslandID(player.getWorld().getName());
            IslandVoter voter = new IslandVoter(player.getUniqueId());
            if(id == Island.NIL_ID){
                player.sendMessage("섬 안에서만 명령어를 실행할수 있습니다.");
                return;
            }

            try {
                if(voter.canVote()){
                    voter.vote(id, value);
                    player.sendMessage("지금 서 있는 섬에 투표했습니다!");
                }else{
                    player.sendMessage("하루에 한번만 투표 가능합니다.");
                }
            }catch (ExecutionException | InterruptedException e){
                player.sendMessage("명령어 실행 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
            }
        }
    }

    protected static class Admin extends BaseCommand {

    }
}
