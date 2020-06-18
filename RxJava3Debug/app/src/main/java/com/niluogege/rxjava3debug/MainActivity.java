package com.niluogege.rxjava3debug;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_transform;

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

        iv_transform = findViewById(R.id.iv_transform);
        findViewById(R.id.btn_transform).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transform();
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
                emitter.onComplete();
            }
        });


        //1. 创建一个 Observer 对象
        //2. 调用到 ObservableCreate 的 subscribeActual 方法进行真正的 订阅
        //3. 创建 CreateEmitter(解释上面的 ObservableEmitter对象) 对 observer 进行包装和增强
        //4. 回调 Observer的 onSubscribe 方法
        //5. 调用 ObservableOnSubscribe 的 subscribe
        //6. 因为 subscribe 中依次调用了 ObservableEmitter 的  onNext 和 onComplete 所以 会一次调用  Observer 的 onNext 和 onComplete
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

    /**
     * 事件变换用例分析
     * subscribe 的时候 这里用了 Consumer  ，当然 也可以直接 new Observer 。
     */
    private void transform() {
        //1. 创建一个 ObservableJust
        //2. 将 item 保存到  ObservableJust 中
        //3. 返回 ObservableJust
        Observable<Integer> observableInteger = Observable.just(R.drawable.ic_launcher);

        //1. 创建一个 ObservableMap
        //2. 将创建的 function 记录到 ObservableMap 中
        //3. 返回 ObservableMap
        Observable<Bitmap> observableBitmap = observableInteger.map(new Function<Integer, Bitmap>() {
            @Override
            public Bitmap apply(Integer integer) throws Throwable {
                return BitmapFactory.decodeResource(getResources(), integer);
            }
        });

        Disposable disposable = observableBitmap.subscribe(new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) throws Throwable {
                iv_transform.setImageBitmap(bitmap);
            }
        });
    }
}
