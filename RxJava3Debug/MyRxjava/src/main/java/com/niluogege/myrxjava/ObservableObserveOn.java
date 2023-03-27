package com.niluogege.myrxjava;

import com.niluogege.myrxjava.scheduler.Scheduler;
import com.niluogege.myrxjava.scheduler.Worker;

import java.util.ArrayDeque;
import java.util.Deque;

public class ObservableObserveOn<T> extends AbstractObservableWithUpStream<T, T> {
    private Scheduler scheduler;

    public ObservableObserveOn(Observable<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }


    @Override
    void subscribeActual(Observer<T> observer) {
        Worker worker = scheduler.createWorker();
        ObserveOnObserver<T> observeOnObserver = new ObserveOnObserver<T>(observer, worker);

        //observeOn 调度的是 onNext，onComplete，等回调方法
        source.subscribe(observeOnObserver);
    }


    static class ObserveOnObserver<T> implements Observer<T>, Runnable {
        private Observer<T> downStream;
        private Worker worker;

        private Deque<T> deque = new ArrayDeque<>();//存放事件的队列
        private volatile boolean done;
        private volatile Throwable error;
        private volatile boolean over;

        public ObserveOnObserver(Observer<T> downStream, Worker worker) {
            this.downStream = downStream;
            this.worker = worker;
        }

        @Override
        public void onSubscribe() {
            downStream.onSubscribe();
        }

        @Override
        public void onNext(T t) {
            //给队列中存入事件
            deque.offer(t);
            //进行线程切换
            worker.schedule(this);
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable error) {

        }

        @Override
        public void run() {
            //队列中获取事件并处理
            while (true){
                T t = deque.poll();

                boolean empty = t == null;
                if (checkTerminated(done, empty, downStream)) {
                    return;
                }
                if (empty)
                    break;
                downStream.onNext(t);
            }
        }

        private boolean checkTerminated(boolean done, boolean empty, Observer<T> downStream) {
            if (over) {
                deque.clear();
                return true;
            }

            if (done) {
                // 如果抛出异常
                if (error != null) {
                    over = true;
                    downStream.onError(error);
                    return true;
                } else if (empty) {
                    over = true;
                    downStream.onComplete();
                    return true;
                }
            }
            return false;
        }
    }
}
