package kr.cosmoisland.cosmoislands.level;

import kr.cosmoisland.cosmoislands.api.level.IslandRewardData;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class RewardDataFactory {

    private final ConcurrentHashMap<String, Class<?>> keyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, RewardDataAdapter<?>> strategies = new ConcurrentHashMap<>();

    public IslandRewardData build(int id, int requiredLevel, Class<?> clazz, String data) throws IllegalArgumentException{
        if(strategies.containsKey(clazz)){
            return strategies.get(clazz).deserialize(id, requiredLevel, data);
        }else{
            throw new IllegalArgumentException("invalid reward type");
        }
    }

    public <T extends IslandRewardData> String serialize(T data){
        if(strategies.containsKey(data.getClass())){
            RewardDataAdapter<T> adapter = (RewardDataAdapter<T>) strategies.get(data.getClass());
            return adapter.serialize(data);
        }else{
            throw new IllegalArgumentException("invalid reward type");
        }
    }

    public void registerAdapter(Class<?> clazz, RewardDataAdapter<?> adapter){
        keyMap.put(clazz.getSimpleName(), clazz);
        strategies.put(clazz, adapter);
    }

    public Class<?> getClassKey(String name){
        return keyMap.get(name);
    }
}
