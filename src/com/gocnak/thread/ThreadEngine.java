package com.gocnak.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 6/10/2015.
 * <p>
 * Handles tasks that should be Asynchronous.
 */
public class ThreadEngine {

    private static ExecutorService pool;

    public static void init() {
        pool = Executors.newCachedThreadPool();
    }

    public static void submit(Runnable r) {
        pool.submit(r);
    }
}