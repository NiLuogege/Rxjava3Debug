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
import android.widget.Toast;

import com.niluogege.rxjava3debug.bean.Arplane;
import com.niluogege.rxjava3debug.bean.Car;
import com.niluogege.rxjava3debug.bean.Transformer;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
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

        findViewById(R.id.btn_transforms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transforms();
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

        //1. 调用三个参数的 subscribe 方法 ,  onError 和 onComplete 会分别被填充为 Functions.ON_ERROR_MISSING 和 Functions.EMPTY_ACTION
        //2. 创建一个 LambdaObserver 用于整合 onNext, onError, onComplete, onSubscribe
        //3. 调用 ObservableMap 的 subscribe 方法开始订阅
        //4. 调用 ObservableMap 的 subscribeActual

        //5. 创建一个 MapObserver 用于封装 下游观察者（LambdaObserver） 和 自身的转换方法
        //6. 调用 ObservableJust 的 subscribe (入参是 MapObserver ) 从而调用到 ObservableJust 的 subscribeActual
        //7. 创建 ScalarDisposable 用于封装 观察者 和 just 中传入的值

        //8. 回调观察者 的 onSubscribe 方法

        //9. 执行 ScalarDisposable 的 run 方法
        //10. 调用 的 MapObserver onNext 方法
        //11. 调用 map 中的 apply 回调方法
        //12. 调用 下游观察者（LambdaObserver）的 onNext方法
        //13. 调用 Consumer 的 accept 方法

        //从这个调用链来看在
        //  1. 订阅操作 过程中 操作流是向上的  真正的观察者（LambdaObserver） 被  MapObserver 封装起来
        //  2. 订阅真正执行的 过程中 操作流程下向下的 先执行 MapObserver 的 onNext 再执行 真正的观察者（LambdaObserver） 的 onNext
        //这个特点现在看来不是很明显 是因为 我们的变换操作太少了，如果有多个变换 那就会很明显

        Disposable disposable = observableBitmap.subscribe(new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) throws Throwable {
                iv_transform.setImageBitmap(bitmap);
            }
        });
    }


    /**
     * 多次事件变换用例分析
     * <p>
     * 该方法的含义是 飞机先变成汽车 ，汽车在变成 变形金刚 ，然后去打仗 （领会精神就行）
     */
    private void transforms() {
        //1. 创建一个 ObservableJust
        //2. 将 item 保存到  ObservableJust 中
        //3. 返回 ObservableJust
        Observable<Arplane> arplaneObservable = Observable.just(new Arplane());

        Observable<Car> carObservable = arplaneObservable.flatMap(new Function<Arplane, ObservableSource<Car>>() {

            @Override
            public ObservableSource<Car> apply(Arplane arplane) throws Throwable {
                @NonNull Observable<Car> carObservable = Observable.just(new Car());
                return carObservable;
            }
        });

        Observable<Transformer> transformerObservable = carObservable.map(new Function<Car, Transformer>() {
            @Override
            public Transformer apply(Car car) throws Throwable {
                return new Transformer();
            }
        });

        transformerObservable.subscribe(new Observer<Transformer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                log("onSubscribe");
            }

            @Override
            public void onNext(@NonNull Transformer transformer) {
                Toast.makeText(MainActivity.this, transformer.fight(), Toast.LENGTH_LONG).show();
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
