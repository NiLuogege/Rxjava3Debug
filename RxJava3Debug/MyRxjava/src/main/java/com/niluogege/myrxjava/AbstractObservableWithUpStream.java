package com.niluogege.myrxjava;


/**
 * 用于上游事件变换的公共父类
 *
 * 这里会将T 类型的数据 变换为 U 类型，
 */
public abstract class AbstractObservableWithUpStream<T,U> extends Observable<U>{
    Observable<T> source;

    public AbstractObservableWithUpStream(Observable<T> source) {
        this.source = source;
    }
}
