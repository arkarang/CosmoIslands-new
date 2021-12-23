package kr.cosmoislands.cosmoislands.core;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandGarbageCollector;
import kr.cosmoislands.cosmoislands.api.IslandService;

import java.util.Hashtable;

public class CosmoIslandGarbageCollector implements IslandGarbageCollector {

    private final IslandService service;
    private final long invalidateLifetimeMills;
    private final Hashtable<Integer, Long> markedIslands;

    CosmoIslandGarbageCollector(IslandService service, long mills){
        this.service = service;
        this.invalidateLifetimeMills = mills;
        this.markedIslands = new Hashtable<>();
    }

    @Override
    public long getInvalidateLifetime() {
        return invalidateLifetimeMills;
    }

    @Override
    public boolean validate(Island island) {
        if(island.isLocal()){
            if(markedIslands.containsKey(island.getId())){
                if (markedIslands.get(island.getId()) > System.currentTimeMillis()) {
                    if(island.validate()){
                        return true;
                    }else{
                        markedIslands.remove(island.getId());
                        return false;
                    }
                }
            }else{
                if(!island.validate())
                    markedIslands.put(island.getId(), invalidateLifetimeMills+System.currentTimeMillis());
            }
        }
        return false;
    }


    @Override
    public void invalidate(Island island) {
        service.unloadIsland(island.getId());
    }
}
