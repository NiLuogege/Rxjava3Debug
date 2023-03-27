package com.niluogege.myrxjava;

/**
 * 观察者顶级接口
 */
public interface Observer<T> {

    void onSubscribe();

    void onNext(T t);

    void onComplete();

    void onError(Throwable error);
}
