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
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

        findViewById(R.id.btn_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduler();
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
//        //1. 创建一个 ObservableJust
//        //2. 将 item 保存到  ObservableJust 中
//        //3. 返回 ObservableJust
//        Observable<Arplane> arplaneObservable = Observable.just(new Arplane());


        //1. 创建一个 ObservableCreate 对象（Observable的子类）
        //2. ObservableOnSubscribe 赋值为 1 中 ObservableCreate的成员变量 source
        //3. 返回 ObservableCreate 对象
        Observable<Arplane> arplaneObservable = Observable.create(new ObservableOnSubscribe<Arplane>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<Arplane> emitter) throws Throwable {
                emitter.onNext(new Arplane());
            }
        });


        //1. 创建一个 ObservableFlatMap 用于封装 上一个 Observable ，变换方法 ，还有一些 配置
        //2. 返回 ObservableFlatMap
        Observable<Car> carObservable = arplaneObservable.flatMap(new Function<Arplane, ObservableSource<Car>>() {

            @Override
            public ObservableSource<Car> apply(Arplane arplane) throws Throwable {
                //创建一个 ObservableJust 并返回
                return Observable.just(new Car());
            }
        });

        //1. 创建一个 ObservableMap
        //2. 将创建的 function 和 上一个 Observable 记录到 ObservableMap 中
        //3. 返回 ObservableMap
        Observable<Transformer> transformerObservable = carObservable.map(new Function<Car, Transformer>() {
            @Override
            public Transformer apply(Car car) throws Throwable {
                return new Transformer();
            }
        });

        //从下到上确定顺序
        //1. 创建一个 Observer 对象
        //2. 调用到 ObservableMap 的 subscribeActual 方法进行真正的 订阅
        //3. 创建一个 MapObserver 用于封装 下游观察者 和 自身的转换方法
        //4. 调用上游 Observable(ObservableFlatMap) 的 subscribe,回到用到 subscribeActual 方法
        //5. 创建一个 MergeObserver 用于封装  下游观察者 , 转换方法 和替他变量
        //6. 调用到 ObservableCreate 的 subscribeActual 方法进行真正的 订阅
        //7. 创建一个 CreateEmitter 用于增强 下游观察者


        //从上到下执行 观察者的 onSubscribe
        //8. 调用 MergeObserver 的 onSubscribe 方法
        //9. 调用 MapObserver 的 onSubscribe 方法
        //10. 调用 1中自己 创建的  Observer 的 onSubscribe 方法


        //11. 执行 我们最开始创建的 ObservableOnSubscribe 的 subscribe 方法

        //从上到下 执行 观察者的 onNext 方法
        //12. 我们执行了  CreateEmitter.onNext （从这里开始就 一路向下 开始执行每一个 observer的 onNext）
        //13. 执行 MergeObserver.onNext
        //14. 触发 apply 方法 ，创建一个 ObservableJust 并返回
        //15. 执行 MergeObserver.subscribeInner
        //16. 执行 MergeObserver.tryEmitScalar
        //17. 执行 MapObserver.onNext
        //18. 执行 apply 变换方法
        //19. 执行 下游观察者（我们自定义的 Observer的）onNext方法

        //从这调用链可以清楚的看到在 subscribe 以后
        //Rxjava内部调用链显示从 下到上确定执行顺序，在 从上到下 真正的执行代码。
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

    /**
     * 简单线程调度
     */
    public void scheduler() {


        //1. 创建一个 ObservableOnSubscribe 对象
        //2. 创建一个 ObservableCreate 对象，并返回
        Observable<Integer> createObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                log("subscribe Thread= " + Thread.currentThread().getName());
                emitter.onNext(R.drawable.ic_launcher);
                emitter.onComplete();
            }
        });

        Observable<Integer> subscribeOnObservable = createObservable.subscribeOn(Schedulers.io());


        Observable<Integer> observeOnObservable = subscribeOnObservable.observeOn(Schedulers.single());


        Observer<Integer> observer = observeOnObservable.subscribeWith(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                log("onSubscribe Thread= " + Thread.currentThread().getName());
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                log("onNext Thread= " + Thread.currentThread().getName() + " onNext= " + integer);
//                Toast.makeText(MainActivity.this, "integer= " + integer, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                log("onError= " + e.getLocalizedMessage());
            }

            @Override
            public void onComplete() {
                log("onComplete Thread= " + Thread.currentThread().getName());
            }
        });
    }
}
