package com.niluogege.myrxjava;

/**
 * 被订阅者的 顶级接口只有 订阅一个方法
 */
public interface ObservableSource<T> {

    void subscribe(Observer<T> observer);

}
