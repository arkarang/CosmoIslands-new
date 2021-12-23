package kr.cosmoislands.cosmoislands.core;


import kr.msleague.mslibrary.database.api.ThrowingConsumer;
import kr.msleague.mslibrary.database.api.ThrowingFunction;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDatabase {

    protected MySQLDatabase database;

    protected AbstractDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public abstract void create();

    public <T> CompletableFuture<T> executeAsync(ThrowingFunction<Connection, T> func){
        return database.executeAsync(func);
    }

    public <R> R execute(ThrowingFunction<Connection, R> function) {
        return database.execute(function);
    }

    public void execute(ThrowingConsumer<Connection> consumer) {
        database.execute(consumer);
    }

}
