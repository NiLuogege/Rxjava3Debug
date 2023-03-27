package com.niluogege.myrxjava;

/**
 * 被订阅者抽象类
 */
public abstract class Observable<T> implements ObservableSource<T> {

    @Override
    public void subscribe(Observer<T> observer) {
        //交给子类自己实现
        subscribeActual(observer);
    }

    abstract void subscribeActual(Observer<T> observer);

    public static <T>  Observable<T> create(ObservableOnSubscribe<T> source){
        return new ObservableCreate<T>(source);
    }

}
