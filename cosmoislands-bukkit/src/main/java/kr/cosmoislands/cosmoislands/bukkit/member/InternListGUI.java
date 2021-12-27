package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InternListGUI extends PlayerListGUI{

    public InternListGUI(UUID owner, List<UUID> list, BukkitExecutor executor) {
        super("알바 목록", get(owner, list), executor);

    }

    private static List<UUID> get(UUID owner, List<UUID> list) {
        List<UUID> uuid = new ArrayList<>();
        uuid.add(owner);
        uuid.addAll(list);
        return uuid;
    }
}
