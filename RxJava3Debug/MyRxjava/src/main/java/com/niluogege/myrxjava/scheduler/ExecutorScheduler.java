package com.niluogege.myrxjava.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorScheduler implements Scheduler{
    @Override
    public Worker createWorker() {
        return new ExecutorSchedulerWorker();
    }

    static class ExecutorSchedulerWorker implements Worker{
        private ExecutorService service = Executors.newCachedThreadPool();

        @Override
        public void schedule(Runnable runnable) {
            service.execute(runnable);
        }
    }
}
