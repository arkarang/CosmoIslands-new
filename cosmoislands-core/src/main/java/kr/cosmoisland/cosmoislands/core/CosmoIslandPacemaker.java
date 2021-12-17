package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandGarbageCollector;
import kr.cosmoisland.cosmoislands.api.IslandPacemaker;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import lombok.val;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CosmoIslandPacemaker implements IslandPacemaker {

    private final long periodMills;                                              // 동작 주기
    private final IslandGarbageCollector islandGC;                               // 섬 가비지 콜렉터
    private final IslandRegistry registry;                                       // 섬 레지스트리, 로컬 섬 정보 얻기 위함.
    private final ExecutorService loop;                                          // 메인 루프 쓰레드 ( 주기반복 )
    private final ExecutorService workers;                                       // 작업 쓰레드풀
    private final ConcurrentHashMap<String, Consumer<Island>> tasks;             // 주기적으로 할 작업 목록
    private final AtomicBoolean run;                                             // 동작 여부
    private final ConcurrentHashMap<Integer, CompletableFuture<?>> runningTasks; // 동작중인 개별 작업들

    CosmoIslandPacemaker(IslandRegistry registry, IslandGarbageCollector gc, ThreadFactory factory, long period){
        this.loop = Executors.newSingleThreadExecutor(factory);
        this.workers = Executors.newFixedThreadPool(Math.max(registry.getAllocatedMaxSize()/4, 1));
        this.periodMills = period;
        this.registry = registry;
        this.tasks = new ConcurrentHashMap<>();
        this.run = new AtomicBoolean(false);
        this.runningTasks = new ConcurrentHashMap<>();
        this.islandGC = gc;
        start();
    }

    private void loop(){
        val islands = registry.getLocals().values();
        islands.forEach(this::submit);
    }

    private void start(){
        run.set(true);
        loop.submit(()->{
            long begin = System.currentTimeMillis();
            while (run.get()){
                loop();
            }
            try {
                CompletableFuture.allOf(runningTasks.values().toArray(new CompletableFuture<?>[0])).get();
            }catch (ExecutionException e){
                e.printStackTrace();
            }catch (InterruptedException ignored){

            }
            long estimatedTime = System.currentTimeMillis() - begin;
            if(estimatedTime < periodMills){
                try {
                    Thread.sleep(periodMills - estimatedTime);
                } catch (InterruptedException ignored) {

                }
            }
        });
    }

    private void submit(Island island){
        val future = CompletableFuture.supplyAsync(()-> {
            tasks.values().forEach(task->task.accept(island));
            if(islandGC.validate(island))
                islandGC.invalidate(island);
            return null;
        }, workers);
        runningTasks.put(island.getId(), future);
        future.thenRun(()->runningTasks.remove(island.getId()));
    }

    @Override
    public long getHeartbeatPeriod() {
        return periodMills;
    }

    @Override
    public void addTask(String tag, Consumer<Island> island) {
        tasks.putIfAbsent(tag, island);
    }

    @Override
    public void shutdown() throws ExecutionException, InterruptedException {
        run.set(false);
        if(!runningTasks.isEmpty()){
            CompletableFuture.allOf(runningTasks.values().toArray(new CompletableFuture<?>[0])).get();
        }
    }
}
