package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmochat.core.api.ChatType;

public final class IslandChatType implements ChatType {

    public static final IslandChatType TOKEN = new IslandChatType();

    private IslandChatType(){}

    @Override
    public String name() {
        return "ISLAND";
    }

    @Override
    public byte number() {
        return 6;
    }
}
