package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmochat.privatechat.PrivateChatDatabase;
import kr.cosmoislands.cosmoislands.api.IslandDataModel;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.val;

import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandChatDataModel extends PrivateChatDatabase implements IslandDataModel {

    private final String islandTable;

    public IslandChatDataModel(MySQLDatabase database, String users, String chats, String islandTable) {
        super(database, users, chats);
        this.islandTable = islandTable;
    }

    @Override
    public void init() {
        this.database.execute((connection) -> {
            PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.chats +
                    " (`id` BIGINT UNIQUE AUTO_INCREMENT, `owner` VARCHAR(36), " +
                    "FOREIGN KEY (`id`) REFERENCES " + islandTable + "(`island_id`) ON DELETE CASCADE, " +
                    "PRIMARY KEY(`id`)) charset=utf8mb4");
            ps2.execute();
            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.users +
                    " (`id` BIGINT, `user` VARCHAR(36), PRIMARY KEY(`user`), " +
                    "FOREIGN KEY (`id`) REFERENCES " + this.chats + "(`id`) ON DELETE CASCADE) charset=utf8mb4");
            ps1.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        val future1 = super.addMember(id, uuid);
        val future2 = super.create(uuid, id);
        return CompletableFuture.allOf(future1, future2);
    }

    @Override
    public CompletableFuture<Void> delete(int id) {
        return super.disband(id);
    }

}
