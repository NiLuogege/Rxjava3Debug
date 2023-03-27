package com.niluogege.myrxjava;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(Emitter<String> emitter) {
                emitter.onNext("aaa");
                emitter.onNext("bbb");
                emitter.onComplete();
                emitter.onNext("ccc");
            }
        }).subscribe(new Observer<String>() {
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
        });
    }
}