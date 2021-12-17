package kr.cosmoisland.cosmoislands.bukkit.listeners;

import com.minepalm.arkarangutils.bukkit.Pair;
import kr.cosmoisland.cosmoislands.bukkit.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class IslandLoadingListener implements Listener {

    HashMap<Class<? extends IslandEvent>, List<Pair<Boolean, Consumer<? extends IslandEvent>>>> map = new HashMap<>();

    public IslandLoadingListener(){
        map.put(IslandCreateEvent.class, new ArrayList<>());
        map.put(IslandDeleteEvent.class, new ArrayList<>());
        map.put(IslandLoadedEvent.class, new ArrayList<>());
        map.put(IslandUnloadEvent.class, new ArrayList<>());
    }

    @EventHandler
    public void onCreate(IslandCreateEvent event){
        run(event);
    }

    @EventHandler
    public void onDelete(IslandDeleteEvent event){
        run(event);
    }

    @EventHandler
    public void onLoad(IslandLoadedEvent event){
        run(event);
    }
    @EventHandler
    public void onUnload(IslandUnloadEvent event){
        run(event);
    }

    public <T extends IslandEvent> void subscribe(Class<T> clazz, boolean instant, Consumer<T> event){
        map.get(clazz).add(new Pair<>(instant, event));
    }


    @SuppressWarnings("unchecked")
    <T extends IslandEvent> void run(T event){
        List<Pair<Boolean, Consumer<? extends IslandEvent>>> list = map.get(event.getClass()), removed = new ArrayList<>();
        for(Pair<Boolean, Consumer<? extends IslandEvent>> pair : list){
            if(pair.getKey())
                removed.add(pair);
            ((Consumer<T>)pair.getValue()).accept(event);
        }
        removed.forEach(list::remove);
    }

}
