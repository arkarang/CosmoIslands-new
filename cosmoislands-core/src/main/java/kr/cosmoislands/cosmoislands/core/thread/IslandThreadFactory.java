package kr.cosmoislands.cosmoislands.core.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class IslandThreadFactory implements ThreadFactory {
    private final ThreadGroup group;

    public static final class IslandThreadGroup extends ThreadGroup{
        private IslandThreadGroup(String name)
        {
            super( name );
        }
    }

    public IslandThreadFactory(String name){
        this.group = new IslandThreadGroup( name );
    }

    @Override
    public Thread newThread(Runnable r){
        return new Thread( group, r );
    }

    public static ThreadFactoryBuilder newFactory(String name){
        return new ThreadFactoryBuilder().setNameFormat( name + " Pool Thread #%1$d" )
                .setThreadFactory(new IslandThreadFactory(name));
    }

    public static ThreadFactoryBuilder newFactory(String name, Thread.UncaughtExceptionHandler handler){
        return new ThreadFactoryBuilder().setNameFormat( name + " Pool Thread #%1$d" )
                .setThreadFactory(new IslandThreadFactory(name)).setUncaughtExceptionHandler(handler);
    }
}
