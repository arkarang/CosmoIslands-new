package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;

import java.util.*;

public class MemberListGUI extends PlayerListGUI{

    public MemberListGUI(Map<UUID, MemberRank> map, BukkitExecutor executor) {
        super("섬원 목록", get(map), provide(executor));

    }

    protected static List<UUID> get(Map<UUID, MemberRank> givenMap){
        List<UUID> result = new ArrayList<>();
        Map<UUID, MemberRank> members = new HashMap<>(givenMap);
        UUID owner = members.entrySet().stream().filter(entry->entry.getValue().equals(MemberRank.OWNER)).findFirst().get().getKey();
        result.add(owner);
        members.remove(owner);
        result.addAll(new ArrayList<>(members.keySet()));
        return result;
    }
}
