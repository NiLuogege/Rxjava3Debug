package com.niluogege.myrxjava;

/**
 * 发射器，内部持有下游 订阅者，可通过发射器简介调用订阅者的方法
 */
public interface Emitter<T> {
    void onNext(T t);

    void onComplete();

    void onError(Throwable error);
}
