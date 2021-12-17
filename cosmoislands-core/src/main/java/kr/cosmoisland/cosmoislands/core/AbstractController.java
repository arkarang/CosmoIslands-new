package kr.cosmoisland.cosmoislands.core;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class AbstractController {

    protected static ExecutorService service = Executors.newScheduledThreadPool(4);
    protected final int id;

    public int getID() {
        return id;
    }

}
