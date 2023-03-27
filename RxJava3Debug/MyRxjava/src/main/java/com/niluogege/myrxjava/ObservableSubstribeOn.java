package com.niluogege.myrxjava;

import com.niluogege.myrxjava.scheduler.Scheduler;

public class ObservableSubstribeOn<T> extends AbstractObservableWithUpStream<T, T> {
    private Scheduler scheduler;

    //传入的为上游被订阅者
    public ObservableSubstribeOn(Observable<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    @Override
    void subscribeActual(Observer<T> observer) {

        observer.onSubscribe();

        SubscribeOnObserver<T> subscribeOnObserver = new SubscribeOnObserver<>(observer);

        SubscribeTask<T> task = new SubscribeTask<T>(source, subscribeOnObserver);

        //真正的订阅要在 scheduler 中进行
        scheduler.createWorker().schedule(task);

    }

    static class SubscribeOnObserver<T> implements Observer<T> {
        Observer<T> downStream;

        public SubscribeOnObserver(Observer<T> observer) {
            downStream = observer;
        }

        @Override
        public void onSubscribe() {

        }

        @Override
        public void onNext(T t) {
            System.out.println("SubscribeOnObserver onNext()" + " threadName=" + Thread.currentThread().getName());
            downStream.onNext(t);
        }

        @Override
        public void onComplete() {
            downStream.onComplete();
        }

        @Override
        public void onError(Throwable error) {
            downStream.onError(error);
        }
    }

    static class SubscribeTask<T> implements Runnable {
        Observer<T> observer;
        Observable<T> source;

        public SubscribeTask(Observable<T> source, Observer<T> observer) {
            this.observer = observer;
            this.source = source;
        }

        @Override
        public void run() {
            System.out.println("SubscribeTask run()" + " threadName=" + Thread.currentThread().getName());
            source.subscribe(observer);
        }
    }
}
