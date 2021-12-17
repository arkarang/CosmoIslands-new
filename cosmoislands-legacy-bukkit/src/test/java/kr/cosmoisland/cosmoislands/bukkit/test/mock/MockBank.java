package kr.cosmoisland.cosmoislands.bukkit.test.mock;

import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockBank implements IslandBank {
    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public List<ItemStack> getContents() {
        return new ArrayList<>();
    }

    @Override
    public CompletableFuture<Void> saveInventory() {
        return null;
    }

    @Override
    public boolean openGUI(Player player) {
        return false;
    }

    @Override
    public CompletableFuture<Double> getMoney() {
        return CompletableFuture.completedFuture(100d);
    }

    @Override
    public CompletableFuture<Void> setMoney(double amount) {
        return null;
    }

    @Override
    public CompletableFuture<Void> addMoney(double amount) {
        return null;
    }

    @Override
    public CompletableFuture<Void> takeMoney(double amount) {
        return null;
    }
}
