package kr.cosmoislands.cosmoislands.bukkit.level;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import kr.cosmoisland.cosmoislands.api.level.IslandRewardData;

import java.util.List;
import java.util.Map;

//요구사항: 보이는 건 로컬캐싱, 실제 지급은 데이터베이스 체크 후 지급
public class AchievementGUI extends ArkarangGUI {

    public AchievementGUI(Map<Integer, Boolean> userdata, List<IslandRewardData> rewardDataList) {
        super(6, "레벨 보상");
        //todo: implements this.
    }
}
