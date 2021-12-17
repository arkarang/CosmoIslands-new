package kr.cosmoisland.cosmoislands.api.bank;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandVault extends IslandComponent {

    CompletableFuture<Double> getMoney();

    CompletableFuture<Void> setMoney(double amount);

    CompletableFuture<Void> addMoney(double amount);

    CompletableFuture<Void> takeMoney(double amount);

}
