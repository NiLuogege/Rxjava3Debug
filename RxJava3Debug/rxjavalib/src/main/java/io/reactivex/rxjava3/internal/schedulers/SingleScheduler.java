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
package io.reactivex.rxjava3.internal.schedulers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * A scheduler with a shared, single threaded underlying ScheduledExecutorService.
 * @since 2.0
 */
public final class SingleScheduler extends Scheduler {

    final ThreadFactory threadFactory;
    final AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>();

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_SINGLE_PRIORITY = "rx3.single-priority";

    private static final String THREAD_NAME_PREFIX = "RxSingleScheduler";

    static final RxThreadFactory SINGLE_THREAD_FACTORY;

    static final ScheduledExecutorService SHUTDOWN;
    static {
        SHUTDOWN = Executors.newScheduledThreadPool(0);
        SHUTDOWN.shutdown();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_SINGLE_PRIORITY, Thread.NORM_PRIORITY)));

        SINGLE_THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority, true);
    }

    public SingleScheduler() {
        this(SINGLE_THREAD_FACTORY);
    }

    /**
     * Constructs a SingleScheduler with the given ThreadFactory and prepares the
     * single scheduler thread.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public SingleScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        executor.lazySet(createExecutor(threadFactory));
    }

    static ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return SchedulerPoolFactory.create(threadFactory);
    }

    @Override
    public void start() {
        ScheduledExecutorService next = null;
        for (;;) {
            ScheduledExecutorService current = executor.get();
            if (current != SHUTDOWN) {
                if (next != null) {
                    next.shutdown();
                }
                return;
            }
            if (next == null) {
                next = createExecutor(threadFactory);
            }
            if (executor.compareAndSet(current, next)) {
                return;
            }

        }
    }

    @Override
    public void shutdown() {
        ScheduledExecutorService current =  executor.getAndSet(SHUTDOWN);
        if (current != SHUTDOWN) {
            current.shutdownNow();
        }
    }

    @NonNull
    @Override
    public Worker createWorker() {
        return new ScheduledWorker(executor.get());
    }

    /**
     * 直接调度 没有延迟
     * @param run the task to schedule
     * @param delay the delay amount, non-positive values indicate non-delayed scheduling
     * @param unit the unit of measure of the delay amount
     * @return
     */
    @NonNull
    @Override
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {

        //1. 调用钩子函数
        //2. 创建 ScheduledDirectTask
        ScheduledDirectTask task = new ScheduledDirectTask(RxJavaPlugins.onSchedule(run));
        try {
            Future<?> f;
            if (delay <= 0L) {//无延迟 直接 submit
                //将 task 添加到 队列中 并返回 执行结果 f
                f = executor.get().submit(task);
            } else {
                f = executor.get().schedule(task, delay, unit);
            }
            //将执行结果 保存到 ScheduledDirectTask 中
            task.setFuture(f);
            // 返回 ScheduledDirectTask
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    @NonNull
    @Override
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
        final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
        if (period <= 0L) {

            ScheduledExecutorService exec = executor.get();

            InstantPeriodicTask periodicWrapper = new InstantPeriodicTask(decoratedRun, exec);
            Future<?> f;
            try {
                if (initialDelay <= 0L) {
                    f = exec.submit(periodicWrapper);
                } else {
                    f = exec.schedule(periodicWrapper, initialDelay, unit);
                }
                periodicWrapper.setFirst(f);
            } catch (RejectedExecutionException ex) {
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            return periodicWrapper;
        }
        ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(decoratedRun);
        try {
            Future<?> f = executor.get().scheduleAtFixedRate(task, initialDelay, period, unit);
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    static final class ScheduledWorker extends Scheduler.Worker {

        final ScheduledExecutorService executor;

        final CompositeDisposable tasks;

        volatile boolean disposed;

        ScheduledWorker(ScheduledExecutorService executor) {
            this.executor = executor;
            this.tasks = new CompositeDisposable();
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            //hook Runnable
            Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

            //将 Runnable 和 tasks 封装成 ScheduledRunnable
            ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, tasks);
            tasks.add(sr);

            try {
                Future<?> f;
                if (delay <= 0L) {
                    //将 任务加入到 线程池中
                    f = executor.submit((Callable<Object>)sr);
                } else {
                    f = executor.schedule((Callable<Object>)sr, delay, unit);
                }

                //将future 设置到 ScheduledRunnable 中
                sr.setFuture(f);
            } catch (RejectedExecutionException ex) {
                dispose();
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            //返回 sr
            return sr;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                tasks.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
