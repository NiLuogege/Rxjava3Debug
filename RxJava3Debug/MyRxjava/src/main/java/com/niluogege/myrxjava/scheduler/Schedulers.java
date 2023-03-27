package com.niluogege.myrxjava.scheduler;

public class Schedulers {
    public static Scheduler MAIN_THREAD;
    public static Scheduler io;

    static {
        MAIN_THREAD = new MainScheduler();
        io = new ExecutorScheduler();
    }
}
