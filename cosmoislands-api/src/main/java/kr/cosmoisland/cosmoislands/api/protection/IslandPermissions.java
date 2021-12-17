package kr.cosmoisland.cosmoislands.api.protection;

public enum IslandPermissions {

    // 4 2 1 0
    WOOD_DOOR,
    IRON_DOOR,
    FENCE_GATE,
    REDSTONE_INTERACTION,
    IGNORE_LOCK,
    //USE_TP, // - 섬 내에서 유저끼리 티피
    //TP_OTHER, // - 섬 내에서 유저 강제 티피
    //SET_JUKE_BOX, // - 마숲꺼 가져와야함ㅋㅋ
    BUILD, // 건축
    OPEN_ISLAND_CHEST,
    USE_CHEST,
    ITEM_PICKUP,
    ITEM_DROP;

}
