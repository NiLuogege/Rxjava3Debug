package com.niluogege.myrxjava;

/**
 * 一般用于转换，
 * 将转入的T类型转换为U类型并进行返回。
 */
public interface Function<T, U> {

    U apply(T t);
}
