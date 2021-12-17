package kr.cosmoisland.cosmoislands.level;

import kr.cosmoisland.cosmoislands.api.level.IslandRewardData;

public interface RewardDataAdapter<T extends IslandRewardData> {

    T deserialize(int id, int requiredLevel, String str);

    String serialize(T t);
}
