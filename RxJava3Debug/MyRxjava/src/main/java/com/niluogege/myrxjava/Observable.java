package com.niluogege.myrxjava;

import com.niluogege.myrxjava.scheduler.Scheduler;

/**
 * 被订阅者抽象类
 */
public abstract class Observable<T> implements ObservableSource<T> {

    @Override
    public void subscribe(Observer<T> observer) {
        System.out.println("---- Observable subscribe() ->" + this+ " threadName=" + Thread.currentThread().getName());
        //交给子类自己实现
        subscribeActual(observer);
    }

    abstract void subscribeActual(Observer<T> observer);

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new ObservableCreate<T>(source);
    }

    public <U> Observable<U> map(Function<T, U> mapper) {
        return new ObservableMap<T, U>(this, mapper);
    }


    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new ObservableSubstribeOn<T>(this, scheduler);
    }

}
