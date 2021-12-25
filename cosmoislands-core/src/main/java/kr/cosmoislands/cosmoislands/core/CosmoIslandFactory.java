package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.ImmutableList;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class CosmoIslandFactory implements IslandFactory {

    private final ExecutorService service;
    private final IslandRegistrationDataModel model;
    private final IslandCloud cloud;
    private final boolean isLocal = true;

    private final LinkedList<String> orders = new LinkedList<>();
    private final ConcurrentHashMap<String, ComponentLifecycle> lifecycles = new ConcurrentHashMap<>();

    public synchronized void addFirst(String tag, ComponentLifecycle strategy) {
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            orders.addFirst(tag);
            lifecycles.put(tag, strategy);
        }

    }

    public synchronized void addLast(String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            orders.addLast(tag);
            lifecycles.put(tag, strategy);
        }
    }

    public synchronized void addBefore(String before, String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            int index = orders.indexOf(before);
            if(index == -1){
                throw new IllegalArgumentException(before+" is not exists");
            }else{
                if(index == 0){
                    orders.addFirst(tag);
                }else {
                    orders.add(index - 1, tag);
                }
                lifecycles.put(tag, strategy);
            }
        }

    }

    public synchronized void addAfter(String after, String tag, ComponentLifecycle strategy){
        if(lifecycles.containsKey(tag)){
            throw new IllegalArgumentException(tag+" is already exists");
        }else {
            int index = orders.indexOf(after);
            if(index == -1){
                throw new IllegalArgumentException(after+" is not exists");
            }else{
                if(index == orders.size()-1){
                    orders.addLast(tag);
                }else {
                    orders.add(index + 1, tag);
                }
                lifecycles.put(tag, strategy);
            }
        }
    }

    public ImmutableList<String> getOrders(){
        return ImmutableList.copyOf(orders);
    }

    public CompletableFuture<IslandContext> fireCreate(UUID uuid){
        CompletableFuture<IslandContext> contextFuture = model.create(uuid).thenComposeAsync(id-> {
            val updateStatusFuture = this.cloud.setStatus(id, IslandStatus.LOADING);
            return updateStatusFuture.thenApply(ignored-> new CosmoIslandContext(id, true));
        }, service);

        return contextFuture.thenApplyAsync(context->{
            DebugLogger.log("create current thread: "+Thread.currentThread().getName());
            try {
                for (ComponentLifecycle strategy : orderedList()) {
                    DebugLogger.log("island factory: execution on create: "+strategy.getClass().getSimpleName());
                    strategy.onCreate(uuid, context).get();
                }
            }catch (InterruptedException | ExecutionException ignored){

            }
            DebugLogger.log("island factory: island creation completed");
            return context;
        }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireLoad(int islandId, boolean isLocal) {
        val updateStatusFuture = this.cloud.setStatus(islandId, IslandStatus.LOADING);
        return CompletableFuture.supplyAsync(()->new CosmoIslandContext(islandId, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenApplyAsync(context->{
                    DebugLogger.log("load current thread: "+Thread.currentThread().getName());
                    try {
                        for (ComponentLifecycle strategy : orderedList()) {
                            strategy.onLoad(context).get();
                        }
                    }catch (InterruptedException | ExecutionException ignored){

                    }
                    return context;
                }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireUnload(Island island) {
        val updateStatusFuture = this.cloud.setStatus(island.getId(), IslandStatus.UNLOADING);
        return CompletableFuture.supplyAsync(() -> new CosmoIslandContext(island, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenApplyAsync(context->{
                    try{
                        for (ComponentLifecycle strategy : orderedList()) {
                            strategy.onUnload(context).get();
                        }
                    }catch (InterruptedException | ExecutionException ignored){

                    }
                    return context;
                }, service);
    }

    @Override
    public CompletableFuture<IslandContext> fireDelete(Island island) {
        val updateStatusFuture = this.cloud.setStatus(island.getId(), IslandStatus.UNLOADING);
        return CompletableFuture.supplyAsync(()->new CosmoIslandContext(island, isLocal), service)
                .thenCombine(updateStatusFuture, (context, ignored) -> context)
                .thenComposeAsync(context->{
                    DebugLogger.log("delete current thread: "+Thread.currentThread().getName());
                    try{
                        for (ComponentLifecycle strategy : orderedList()) {
                            DebugLogger.log("island factory: execution on delete: "+strategy.getClass().getSimpleName());
                            strategy.onDelete(context).get();
                        }
                    }catch (InterruptedException | ExecutionException ignored){

                    }
                    return model.delete(island.getId()).thenApply(ignored->context);
                }, service);
    }

    private List<ComponentLifecycle> orderedList(){
        List<ComponentLifecycle> list = new ArrayList<>();
        for (String order : orders) {
            ComponentLifecycle strategy = lifecycles.get(order);
            if(strategy != null){
                list.add(strategy);
            }
        }
        return list;
    }


    // ---------------------

    /*
    private void initPreCreate(){
        BiPipeline<Island, UUID> biPipeline = new BiPipeline<>();
        biPipeline.addLast("create-player-map", (island, uuid)-> database.getLoader(PlayersMapLoader.class).create(island.getID(), uuid).get());
        biPipeline.addLast("create-bank", (island, uuid) -> database.getLoader(BankMoneyLoader.class).create(island.getID()).get());
        biPipeline.addLast("create-data", (island, uuid) -> database.getLoader(IslandDataLoader.class).create(island.getID()).get());
        biPipeline.addLast("create-chat", (island, uuid) -> database.getLoader(IslandChatLoader.class).create(island.getID(), uuid).get());
        biPipeline.addLast("create-world", (island, uuid) -> ManyWorlds.createNewWorld(new ManyWorldInfo(WorldToken.get("ISLAND"), "island", island.getWorldName()), ()->{
            try {
                getPostCreatePipeline().run(island, uuid);
                Optional.ofNullable(Bukkit.getPlayer(uuid)).ifPresent(player-> player.sendMessage("섬 생성이 완료 되었습니다!"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).get());
        biPipelines.put(PRE_CREATE, biPipeline);
    }

    private void initPostCreate(){
        BiPipeline<Island, UUID> biPipeline = new BiPipeline<>();
        biPipeline.addLast("load-data", (island, uuid) -> island.getData().get());
        biPipeline.addLast("load-player-map", (island, uuid) -> island.getPlayersMap().get());
        biPipeline.addLast("load-bank", (island, uuid) -> island.getBank().get());
        biPipeline.addLast("load-achievements", (island, uuid) -> island.getAchievementData().get());
        biPipeline.addLast("load-interns", (island, uuid) -> island.getInternsMap().get());
        biPipeline.addLast("run-post-load", (island, uuid) -> getPostLoadPipeline().run(island));
        biPipeline.addLast("complete-create", (island, uuid) -> updateIsland(island.getID(), IslandTracker.ONLINE));
        biPipelines.put(POST_CREATE, biPipeline);
    }

    private void initPreLoad(){
        Pipeline<Island> pipeline = new Pipeline<>();
        pipeline.addLast("load-data", island-> island.getData().get());
        pipeline.addLast("load-player-map", island -> island.getPlayersMap().get());
        pipeline.addLast("load-bank", island -> island.getBank().get());
        pipeline.addLast("load-achievements", island -> island.getAchievementData().get());
        pipeline.addLast("load-interns", island -> island.getInternsMap().get());
        pipeline.addLast("load-chat", island -> island.getChat().get());
        pipeline.addLast("load-world", island -> {
            if(island instanceof IslandLocal) {
                WorldInfo info = ManyWorlds.getWorldDatabase(WorldToken.get("ISLAND")).getWorldInfo(island.getWorldName()).get();
                IslandTracker tracker = trackIsland(island.getID());
                if (!tracker.statusEquals(IslandTracker.ONLINE).get() && info != null) {
                    ManyWorlds.loadWorld(new ManyWorldInfo(WorldToken.get("ISLAND"), "island", island.getWorldName(), 0L, "sea-generator"), ()->{
                        try {
                            getPostLoadPipeline().run(island);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).get();
                }
            }
        });
        pipelines.put(PRE_LOAD, pipeline);
    }

    private void initPostLoad(){
        Pipeline<Island> pipeline = new Pipeline<>();
        pipeline.addLast("set-game-rule", island -> {
            if(island instanceof IslandLocal){
                IslandLocal local = (IslandLocal)island;
                local.getWorld().ifPresent(world -> {
                    world.setGameRuleValue("keepInventory", "true");
                    world.setGameRuleValue("doDaylightCycle", "false");
                    world.setGameRuleValue("doWeatherCycle", "false");
                    world.setGameRuleValue("doMobSpawning", "false");
                    world.setGameRuleValue("mobGriefing", "false");
                });
            }
        });
        pipeline.addLast("apply-settings", island -> {
            if(island instanceof IslandLocal){
                IslandLocal local = (IslandLocal)island;
                local.getWorld().ifPresent(world -> {
                    String time = "8000", sunny = "true";
                    try {
                        IslandSettingsMap map = local.getSettings().get();
                        time = map.getSetting(IslandSettings.TIME).get();
                        sunny = map.getSetting(IslandSettings.SUNNY).get();
                    }catch ( InterruptedException | ExecutionException e){

                    }
                    try{
                        int i = Integer.parseInt(time);
                        world.setTime(i);
                    }catch (IllegalArgumentException ignored){

                    }
                    try{
                        boolean b = Boolean.parseBoolean(sunny);
                        world.setStorm(!b);
                    }catch (IllegalArgumentException ignored){

                    }
                });

            }
        });
        pipeline.addLast("load-complete", island -> updateIsland(island.getID(), IslandTracker.ONLINE));
        pipelines.put(POST_LOAD, pipeline);
    }

    private void initUnload(){
        Pipeline<Island> pipeline = new Pipeline<>();
        pipeline.addLast("unload-bank",island -> database.getLoader(BankMoneyLoader.class).save(island.getID(), island.getBank().get()));
        pipeline.addLast("unload-world", island -> {
            if(island instanceof IslandLocal) {
                IslandLocal local = (IslandLocal)island;
                ManyWorlds.unloadWorld(local.getWorldName());
                database.getLoader(IslandTrackerLoader.class).unregisterIsland(island.getID());
            }
        });
        pipeline.addLast("unload-tracker", island -> database.getLoader(IslandTrackerLoader.class).unregisterIsland(island.getID()));
        pipelines.put(UNLOAD, pipeline);
    }

    private void initDelete(){
        Pipeline<Island> pipeline = new Pipeline<>();
        pipeline.addLast("unload-island", island -> unloadIsland0(island.getID()));
        pipeline.addLast("delete-registration-and-sync-player", island -> {
            List<IslandPlayer> players = database.getLoader(PlayersMapLoader.class).getPlayers(island.getID()).get();
            List<UUID> list = players.stream().map(IslandPlayer::getUniqueID).collect(Collectors.toList());
            database.getLoader(IslandRegistrationLoader.class).delete(island.getID()).get();
            CosmoIslands.getInst().syncPlayer(list);
            CosmoIslands.sendAll(new IslandPlayerSyncPacket(IslandPlayerSyncPacket.PLAYER, list));
        });
        pipeline.addLast("delete-world", island -> ManyWorlds.getWorldDatabase(WorldToken.get("ISLAND")).deleteWorld(new ManyWorldInfo(WorldToken.get("ISLAND"), "island", "island_"+island.getWorldName())));
        pipelines.put(DELETE, pipeline);
    }



    @Override
    public CompletableFuture<IslandPlayersMap> getPlayersMap() {
        if(!components.containsKey(PlayersMap.class)) {
            return CompletableFuture.supplyAsync(()->{
                try {
                    PlayersMapLoader loader = database.getLoader(PlayersMapLoader.class);
                    IslandPlayersMap initial = loader.load(id).get();
                    PlayersMap result = new PlayersMap(initial, new PlayersMapController(id));
                    components.put(IslandPlayersMap.class, result);
                    return result;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandPlayersMap)components.get(IslandPlayersMap.class));
        }
    }

    @Override
    public CompletableFuture<IslandPermissionsMap> getPermissionsMap() {
        if(!components.containsKey(IslandPermissionsMap.class)){
            components.put(IslandPermissionsMap.class, new FixedPermissionsMap(CosmoIslands.getInst().getGlobalConfig()));
        }
        return CompletableFuture.completedFuture((IslandPermissionsMap) components.get(IslandPermissionsMap.class));
    }

    @Override
    public CompletableFuture<IslandData> getData() {
        if(!components.containsKey(IslandData.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    CosmoIslandData result;
                    IslandDataLoader loader = database.getLoader(IslandDataLoader.class);
                    IslandData initial = loader.load(id).get();
                    result = new CosmoIslandData(initial, new IslandDataController(id));
                    components.put(IslandData.class, result);
                    return result;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandData) components.get(IslandData.class));
        }
    }

    @Override
    public CompletableFuture<IslandBank> getBank() {
        if(!components.containsKey(IslandBank.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    IslandBank bank = database.getLoader(BankMoneyLoader.class).load(id).get();
                    components.put(IslandBank.class, (Bank)bank);
                    return bank;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandBank)components.get(IslandBank.class));
        }
    }

    @Override
    public CompletableFuture<IslandChat> getChat() {
        if(!components.containsKey(IslandChat.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    IslandChat chat = database.getLoader(IslandChatLoader.class).getChat(this.id).get();
                    components.put(IslandChat.class, chat);
                    return chat;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else
            return CompletableFuture.completedFuture((IslandChat)components.get(IslandChat.class));
    }

    @Override
    public CompletableFuture<IslandRewardsData> getAchievementData() {
        if(!components.containsKey(IslandRewardsData.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    IslandRewardsData data = database.getLoader(IslandRewardDataLoader.class).load(id).get();
                    RewardData result = new RewardData(data, new RewardDataController(id));
                    components.put(IslandRewardsData.class, result);
                    return data;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandRewardsData)components.get(IslandRewardsData.class));
        }
    }

    @Override
    public CompletableFuture<IslandInternsMap> getInternsMap() {
        if(!components.containsKey(IslandInternsMap.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    IslandInternsMap map = database.getLoader(InternsMapLoader.class).load(id).get();
                    InternsMap result = new InternsMap(map, new InternsMapController(id));
                    components.put(IslandInternsMap.class, result);
                    return map;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandInternsMap)components.get(IslandInternsMap.class));
        }
    }

    @Override
    public CompletableFuture<IslandSettingsMap> getSettings() {
        if(!components.containsKey(IslandSettingsMap.class)){
            return CompletableFuture.supplyAsync(()->{
                try{
                    IslandSettingsMap map = database.getLoader(SettingsLoader.class).load(id).get();
                    SettingsMap result = new SettingsMap(map, new SettingsMapController(id));
                    components.put(IslandSettingsMap.class, result);
                    return map;
                }catch (ExecutionException e){
                    e.getCause().printStackTrace();
                }catch (InterruptedException ignored){

                }
                return null;
            }, service);
        }else{
            return CompletableFuture.completedFuture((IslandSettingsMap)components.get(IslandSettingsMap.class));
        }
    }
    */
}
