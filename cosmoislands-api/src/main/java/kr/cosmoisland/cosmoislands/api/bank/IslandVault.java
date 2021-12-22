package kr.cosmoisland.cosmoislands.api.bank;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandVault extends IslandComponent {

    byte COMPONENT_ID = 2;

    CompletableFuture<Double> getMoney();

    CompletableFuture<Void> setMoney(double amount);

    CompletableFuture<Void> addMoney(double amount);

    CompletableFuture<Void> takeMoney(double amount);

}
