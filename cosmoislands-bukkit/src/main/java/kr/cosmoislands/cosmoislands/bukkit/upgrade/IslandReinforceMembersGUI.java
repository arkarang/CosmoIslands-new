package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.bukkit.config.ReinforceSetting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class IslandReinforceMembersGUI extends ReinforceGUI<IslandData> {

    private final IslandBank bank;

    public IslandReinforceMembersGUI(ReinforceSetting setting, IslandBank bank, IslandData islandData) {
        super("섬 강화: 최대 인원", setting, islandData);
        this.bank = bank;
        for(int i = 0; i < setting.getMaxLevel(); i++) {
            final int level = i + 1;
            icons.put(i, () -> {
                ReinforceSetting.Component comp = setting.getComponent(level);
                ItemStack item = setting.getComponent(level).getItem().clone();
                return new ItemStackBuilder(item).setName("섬 최대 인원 : " + comp.getValue() + "명").addLine("§a§l 가격 §f: §e" + getCost(level) + "G").getHandle();
            });
        }
        init();
    }

    @Override
    boolean canLevelUp(IslandData islandData, int cl) throws ExecutionException, InterruptedException {
        return getCurrentLevel(islandData) == cl - 1;
    }

    @Override
    boolean hasCost(int lv, IslandData islandData) throws ExecutionException, InterruptedException {
        return setting.getComponent(lv).getCost() <= bank.getMoney().get();
    }

    @Override
    void takeAndLevelUp(int lv, IslandData islandData) throws ExecutionException, InterruptedException {
        bank.takeMoney(getCost(lv)).get();
        islandData.setMaxInterns(setting.getComponent(lv).getValue());
    }
    @Override
    void executeNotEnoughMoney(Player player, int cl) {
        closeAndSend(player, "당신의 섬은 강화하는데 필요한 돈이 부족합니다. 요구 비용:" + getCost(cl));
    }

    @Override
    void executeNotEnoughLevel(Player player, int cl) {
        closeAndSend(player, "섬 레벨이 충족되지 않았습니다. 요구 멤버 레벨: " + cl);
    }

    @Override
    void executeSuccessful(Player player, int cl) {
        closeAndSend(player, "섬 레벨을 올렸습니다! " + (cl-1) + " -> " + cl);
    }

    @Override
    int getClickedSlotToLevel(int i) {
        return i+1;
    }

    @Override
    int getCurrentLevel(IslandData islandData) throws ExecutionException, InterruptedException {
        for(int k = 0; k <= setting.getMaxLevel(); k ++){
            if( islandData.getMaxPlayers().get() <= setting.getComponent(k).getValue())
                return setting.getComponent(k).getLevel();
        }
        return 0;
    }

    @Override
    void executeAlreadyReached(Player player, int cl) throws ExecutionException, InterruptedException {
        player.sendMessage("이미 강화했습니다.");
    }
}
