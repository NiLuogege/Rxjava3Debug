package com.niluogege.myrxjava;

/**
 * 用于链接 被订阅者和发射器的

 * 主要通过 subscribe 方法链接
 */
public interface ObservableOnSubscribe<T> {

    void subscribe(Emitter<T> emitter);
}
