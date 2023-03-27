package com.niluogege.myrxjava;

/**
 * 一个比较重要的 被订阅者，用于事件的发出
 */
public class ObservableCreate<T> extends Observable<T> {

    //上游被订阅者，这个一般就是顶层被订阅者了。就是事件产生的地方
    private ObservableOnSubscribe<T> source;


    public ObservableCreate(ObservableOnSubscribe<T> source) {
        System.out.println("---- ObservableCreate构造方法 ->" + this);
        //上游被订阅者，这个一般就是顶层被订阅者了。就是事件产生的地方
        this.source = source;
    }

    //当代码流执行到 subscribe的时候会触发。
    @Override
    void subscribeActual(Observer<T> observer) {
        //调用下游observer的订阅方法
        observer.onSubscribe();

        //创建一个发射器将 下游observer 穿进去
        CreateEmitter<T> emitter = new CreateEmitter<>(observer);

        //调用上游被订阅者的订阅方法 并传入新建的Emitter，将订阅操作向上推进。
        source.subscribe(emitter);
    }

    static class CreateEmitter<T> implements Emitter<T> {
        Observer<T> downStream;
        boolean done = false;

        public CreateEmitter(Observer<T> downStream) {
            this.downStream = downStream;
        }

        @Override
        public void onNext(T t) {
            if (done) return;
            System.out.println("---- onNext ->" + this);
            downStream.onNext(t);
        }

        @Override
        public void onComplete() {
            if (done) return;
            downStream.onComplete();
            done = true;
        }

        @Override
        public void onError(Throwable error) {
            if (done) return;
            downStream.onError(error);
            done = true;
        }
    }
}
