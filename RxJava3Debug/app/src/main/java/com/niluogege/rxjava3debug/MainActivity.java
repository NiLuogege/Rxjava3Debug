package com.niluogege.rxjava3debug;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private void log(String str) {
        Log.e("MainActivity", "str= " + str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_simple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simple();
            }
        });


    }

    /**
     * 最简单的用例分析
     */
    private void simple() {
        //1. 创建一个 ObservableCreate 对象（Observable的子类）
        //2. ObservableOnSubscribe 赋值为 1 中 ObservableCreate的成员变量 source
        //3. 返回 ObservableCreate 对象
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                emitter.onNext("hello rxjava3");
            }
        });


        //1. 创建一个 Observer 对象
        Observer<String> observer = observable.subscribeWith(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                log("onSubscribe");
            }

            @Override
            public void onNext(@NonNull String s) {
                log("onNext= " + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                log("onError= " + e.getLocalizedMessage());

            }

            @Override
            public void onComplete() {
                log("onComplete");
            }
        });
    }
}
