package com.niluogege.myrxjava.scheduler;

/**
 * 这里会真的进行线程调度
 */
public interface Worker {
    //将 Runnable 进行线程调度
    void schedule(Runnable runnable);
}
