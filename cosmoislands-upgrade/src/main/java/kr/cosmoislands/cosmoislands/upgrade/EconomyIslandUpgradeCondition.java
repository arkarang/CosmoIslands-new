package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.val;

import java.util.concurrent.CompletableFuture;

public abstract class EconomyIslandUpgradeCondition extends AbstractIslandUpgradeCondition{

    public EconomyIslandUpgradeCondition(IslandUpgradeType type, CompletableFuture<IslandUpgradeSettings> settings, Island island) {
        super(type, settings, island);
    }

    @Override
    public CompletableFuture<Boolean> canUpgrade() {
        IslandVault vault = island.getComponent(IslandVault.class);
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);

        val currentLevelFuture = upgrade.getLevel(type);
        val currentMoneyFuture = vault.getMoney();
        val reachedMaxLevelFuture = isReachedMaxLevelInternally(currentLevelFuture);

        return reachedMaxLevelFuture.thenCompose(reachedMaxLevel->{
            if(!reachedMaxLevel){
                return hasCostInternally(currentLevelFuture, currentMoneyFuture);
            }else {
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasCost() {
        IslandVault vault = island.getComponent(IslandVault.class);
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);

        val currentLevelFuture = upgrade.getLevel(type);
        val currentMoneyFuture = vault.getMoney();

        return hasCostInternally(currentLevelFuture, currentMoneyFuture);
    }

    private CompletableFuture<Boolean> hasCostInternally(CompletableFuture<Integer> currentLevelFuture,
                                                         CompletableFuture<Double> currentMoneyFuture){
        return getSettings().thenCompose(settings -> {
            return currentLevelFuture.thenCombine(currentMoneyFuture, (level, money)->{
                final int nextLevel = level+1;
                return money >= settings.getRequiredCost(nextLevel);
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> isReachedMaxLevel(){
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);
        val currentLevelFuture = upgrade.getLevel(type);
        return isReachedMaxLevelInternally(currentLevelFuture);
    }

    private CompletableFuture<Boolean> isReachedMaxLevelInternally(CompletableFuture<Integer> currentLevelFuture){
        return getSettings().thenCompose(settings -> {
            return currentLevelFuture.thenApply(level -> level >= settings.getMaxLevel());
        });
    }

    @Override
    public CompletableFuture<Result> upgrade() {
        IslandVault vault = island.getComponent(IslandVault.class);
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);

        val currentLevelFuture = upgrade.getLevel(type);
        val currentMoneyFuture = vault.getMoney();
        val reachedMaxLevelFuture = isReachedMaxLevelInternally(currentLevelFuture);
        val hasCostFuture = hasCostInternally(currentLevelFuture, currentMoneyFuture);

        return reachedMaxLevelFuture.thenCombine(hasCostFuture, (reachedMaxLevel, hasCost)->{
            if(!reachedMaxLevel){
                if(hasCost){
                    val future = executeUpgrade(island, currentLevelFuture).thenApply(ignored->Result.SUCCESSFUL);
                    return future;
                }else{
                    return CompletableFuture.completedFuture(Result.NOT_ENOUGH_MONEY);
                }
            }else
                return CompletableFuture.completedFuture(Result.REACHED_MAX_LEVEL);
        }).thenCompose(future->future);
    }

    private CompletableFuture<Void> executeUpgrade(Island island, CompletableFuture<Integer> levelFuture){
        IslandVault vault = island.getComponent(IslandVault.class);
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);

        return getSettings().thenCompose(settings->{
            return levelFuture.thenCompose(level->{
                final int nextLevel = level+1;
                double cost = settings.getRequiredCost(nextLevel);

                val takeMoneyFuture = vault.takeMoney(cost);
                val executeUpgradeFuture = executeUpgrade(island, settings.getValue(nextLevel));
                val setLevelFuture = upgrade.setLevel(type, nextLevel);

                return CompletableFuture.allOf(takeMoneyFuture, executeUpgradeFuture, setLevelFuture);
            });
        });
    }

    protected abstract CompletableFuture<Void> executeUpgrade(Island island, int value);
}
