package kr.cosmoisland.cosmoislands.bungee;

import javax.annotation.Nullable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ServerSelector{

    private final Hashtable<String, Boolean> storageServers = new Hashtable<>();
    final int maxIslands;
    final LinkedList<Predicate<BukkitView>> filters = new LinkedList<>();
    final GlobalDatabase worldGlobal;

    ServerSelector(GlobalDatabase database, int maxIslands, List<String> serverList){
        this.worldGlobal = database;
        this.maxIslands = maxIslands;
        serverList.forEach(server->storageServers.put(server, true));
    }

    void initFilters(){
        filters.add(view -> view.getLoadedWorlds() <= maxIslands);
        filters.add(view -> storageServers.containsKey(view.getServerName()));
    }

    @Nullable
    public BukkitView getAtLeast() throws ExecutionException, InterruptedException {
        List<BukkitView> list = worldGlobal.getServers().get();
        list = filter(list);
        BukkitView result = null;

        for (BukkitView view : list) {
            result = result == null ? view : result;
            result = result.getLoadedWorlds() > view.getLoadedWorlds() ? view : result;
        }
        return result;
    }

    List<BukkitView> filter(List<BukkitView> list){
        Stream<BukkitView> stream = list.stream();
        for (Predicate<BukkitView> filter : filters) {
            stream = stream.filter(filter);
        }
        return stream.collect(Collectors.toList());
    }
}
