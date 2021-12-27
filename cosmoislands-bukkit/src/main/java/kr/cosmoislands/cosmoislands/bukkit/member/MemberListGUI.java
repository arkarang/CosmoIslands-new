package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;

import java.util.*;

public class MemberListGUI extends PlayerListGUI{

    public MemberListGUI(Map<UUID, MemberRank> map, BukkitExecutor executor) {
        super("섬원 목록", get(map), executor);

    }

    protected static List<UUID> get(Map<UUID, MemberRank> givenMap){
        List<UUID> result = new ArrayList<>();
        Map<UUID, MemberRank> members = new HashMap<>(givenMap);
        Optional<Map.Entry<UUID, MemberRank>> ownerOptional = members.entrySet().stream()
                .filter(entry-> entry.getValue().getPriority() >= MemberRank.MEMBER.getPriority())
                .filter(entry->entry.getValue().equals(MemberRank.OWNER))
                .findFirst();

        if(ownerOptional.isPresent()) {
            result.add(ownerOptional.get().getKey());
            members.remove(ownerOptional.get().getKey());
        }else{
            result.add(SYSTEM);
        }
        result.addAll(new ArrayList<>(members.keySet()));
        return result;
    }
}
