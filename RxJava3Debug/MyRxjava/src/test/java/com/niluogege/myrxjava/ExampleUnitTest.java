package com.niluogege.myrxjava;

import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onSubscribe() {
            System.out.println("onSubscribe " + " threadName=" + Thread.currentThread().getName());
        }

        @Override
        public void onNext(String s) {
            System.out.println("onNext s=" + s + " threadName=" + Thread.currentThread().getName());

        }

        @Override
        public void onComplete() {
            System.out.println("onComplete " + " threadName=" + Thread.currentThread().getName());

        }

        @Override
        public void onError(Throwable error) {
            System.out.println("onError " + " threadName=" + Thread.currentThread().getName());

        }
    };

    /**
     * 最简单的使用，
     * 其实就是通过 ObservableOnSubscribe 和 Emitter 将 订阅者和被订阅者链接起来
     */
    @Test
    public void simpleTest() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(Emitter<String> emitter) {
                emitter.onNext("aaa");
                emitter.onNext("bbb");
                emitter.onComplete();
                emitter.onNext("ccc");
            }
        }).subscribe(observer);
    }


    /**
     * 事件变换测试
     */
    @Test
    public void mapTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(Emitter<Integer> emitter) {
                emitter.onNext(111);
                emitter.onNext(222);
                emitter.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer s) {
                return "变换啦" + s;
            }
        }).subscribe(observer);
    }
}