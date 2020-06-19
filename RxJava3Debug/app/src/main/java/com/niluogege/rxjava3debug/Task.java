package com.niluogege.rxjava3debug;

import android.os.SystemClock;

import java.util.concurrent.Callable;

public class Task implements Callable<String> {

    @Override
    public String call() throws Exception {

        System.out.println("Thread= " + Thread.currentThread().getName());

        SystemClock.sleep(1000);

        System.out.println(" sleep 结束");

        return "task task task ";
    }
}