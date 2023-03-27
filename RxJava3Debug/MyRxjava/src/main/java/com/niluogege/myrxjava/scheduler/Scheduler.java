package com.niluogege.myrxjava.scheduler;

/**
 * 调度器的公共接口
 */
public interface Scheduler {
    //需要返回一个Worker
    Worker createWorker();
}
