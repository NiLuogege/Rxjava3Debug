package com.niluogege.myrxjava.scheduler;

public class MainScheduler implements Scheduler{
    @Override
    public Worker createWorker() {
        return new MainWorker();
    }

    static class MainWorker implements Worker{

        @Override
        public void schedule(Runnable runnable) {
            new Thread(runnable,"Main").start();
        }
    }
}
