/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.observers.BasicFuseableObserver;

import java.util.Objects;

public final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {
    final Function<? super T, ? extends U> function;

    public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {
        //记录上一个 Observable
        super(source);
        //记录传入的 function
        this.function = function;
    }

    /**
     * @param t 真实的观察者
     */
    @Override
    public void subscribeActual(Observer<? super U> t) {
        //1. 创建一个 MapObserver 用于封装 下游观察者 和 自身的转换方法
        //2. 调用上游 Observable 的 subscribe
        source.subscribe(new MapObserver<T, U>(t, function));
    }

    static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {
        final Function<? super T, ? extends U> mapper;

        /**
         * @param actual 真实的观察者
         * @param mapper 转换方法
         */
        MapObserver(Observer<? super U> actual, Function<? super T, ? extends U> mapper) {
            //actual 会赋值给 父类的 downstream
            super(actual);
            //记录转换方法
            this.mapper = mapper;
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                downstream.onNext(null);
                return;
            }

            U v;

            try {
                //调用 调用真正的 的 apply方法
                v = Objects.requireNonNull(mapper.apply(t), "The mapper function returned a null value.");
            } catch (Throwable ex) {
                fail(ex);
                return;
            }

            //调用下游订阅者 的 onNext
            downstream.onNext(v);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public U poll() throws Throwable {
            T t = qd.poll();
            return t != null ? Objects.requireNonNull(mapper.apply(t), "The mapper function returned a null value.") : null;
        }
    }
}
