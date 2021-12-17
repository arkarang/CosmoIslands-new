package kr.cosmoislands.cosmoislands.bank;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.bank.IslandVault;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandVault implements IslandVault {

    private final int islandId;
    private final IslandVaultDataModel model;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public CompletableFuture<Double> getMoney() {
        return model.getMoney(islandId);
    }

    @Override
    public CompletableFuture<Void> setMoney(double amount) {
        return model.setMoney(islandId, amount);
    }

    @Override
    public CompletableFuture<Void> addMoney(double amount) {
        return model.addMoney(islandId, amount);
    }

    @Override
    public CompletableFuture<Void> takeMoney(double amount) {
        return model.takeMoney(islandId, amount);
    }
}
