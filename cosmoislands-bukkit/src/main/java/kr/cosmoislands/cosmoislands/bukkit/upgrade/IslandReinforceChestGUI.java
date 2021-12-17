package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.config.ReinforceSetting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class IslandReinforceChestGUI extends ReinforceGUI<IslandBank> {

    public IslandReinforceChestGUI(ReinforceSetting setting, IslandBank bank) {
        super("섬 강화: 창고", setting, bank);
        if (CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().isEnable("chest")) {
            for (int i = 0; i < setting.getMaxLevel(); i++) {
                final int level = i + 1;
                icons.put(i, () -> {
                    ItemStack item = setting.getComponent(level).getItem().clone();
                    return new ItemStackBuilder(item).setName("섬 창고 " + ReinforceGUI.getKoreanOrdering(setting.getComponent(level).getValue()) + "줄").addLine("§a§l 가격 §f: §e" + getCost(level) + "G").getHandle();
                });
            }
        }
        init();
    }

    @Override
    int getClickedSlotToLevel(int i) {
        return i + 1;
    }

    @Override
    void executeAlreadyReached(Player player, int cl) throws ExecutionException, InterruptedException {
        player.sendMessage("이미 강화했습니다.");
    }

    @Override
    int getCurrentLevel(IslandBank islandBank) throws ExecutionException, InterruptedException {
        return islandBank.getLevel();
    }

    @Override
    boolean canLevelUp(IslandBank bank, int cl) throws ExecutionException, InterruptedException {
        return getCurrentLevel(bank) == cl - 1;
    }

    @Override
    boolean hasCost(int lv, IslandBank bank) throws ExecutionException, InterruptedException {
        return bank.getMoney().get() >= getCost(lv);
    }

    @Override
    void takeAndLevelUp(int lv, IslandBank bank) {
        bank.setLevel(lv);
        bank.takeMoney(getCost(lv));
    }

    @Override
    void executeNotEnoughMoney(Player player, int cl) {
        closeAndSend(player, "당신의 섬은 강화하는데 필요한 돈이 부족합니다. 요구 비용:" + getCost(cl));
    }

    @Override
    void executeNotEnoughLevel(Player player, int cl) {
        closeAndSend(player, "섬 레벨이 충족되지 않았습니다. 요구 창고 레벨: " + cl);
    }

    @Override
    void executeSuccessful(Player player, int cl) {
        closeAndSend(player, "섬 레벨을 올렸습니다! " + (cl-1) + " -> " + cl);
    }


}
