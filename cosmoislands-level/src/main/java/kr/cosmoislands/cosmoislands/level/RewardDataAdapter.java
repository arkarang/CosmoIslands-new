package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.level.IslandRewardData;

public interface RewardDataAdapter<T extends IslandRewardData> {

    T deserialize(int id, int requiredLevel, String str);

    String serialize(T t);
}
