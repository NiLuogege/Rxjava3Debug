package com.niluogege.myrxjava;

/**
 * map操作符的实现， 主要用过一个Function类进行变换
 */
public class ObservableMap<T, U> extends AbstractObservableWithUpStream<T, U> {

    Function<T, U> mapper;

    //传入上游被订阅者
    public ObservableMap(Observable<T> source, Function<T, U> mapper) {
        super(source);
        this.mapper = mapper;
    }

    //传入的是下游订阅者
    @Override
    void subscribeActual(Observer<U> observer) {
        ObserverMap<T, U> observerMap = new ObserverMap<>(observer, mapper);
        source.subscribe(observerMap);

    }

    static class ObserverMap<T, U> implements Observer<T> {
        Observer<U> downStream;
        Function<T, U> mapper;


        public ObserverMap(Observer<U> observer, Function<T, U> mapper) {
            this.downStream = observer;
            this.mapper = mapper;
        }

        @Override
        public void onSubscribe() {
            downStream.onSubscribe();
        }

        @Override
        public void onNext(T t) {
            downStream.onNext(mapper.apply(t));
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
}
