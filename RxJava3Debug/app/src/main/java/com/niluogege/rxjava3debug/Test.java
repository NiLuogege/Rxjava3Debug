package com.niluogege.rxjava3debug;


import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Test {
    ExecutorService service = Executors.newCachedThreadPool();

    public Test() {

    }

    public void startTask() {
        try {
            Future<String> future = service.submit(new Task());

            System.out.println("任务结束前" + " Thread= " + Thread.currentThread().getName());

            String s = future.get();


            System.out.println("任务结束= " + s + " Thread= " + Thread.currentThread().getName());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
