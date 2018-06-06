package com.ruanchao.app2.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by ruanchao on 2018/5/28.
 */

public class DynamicProxy implements InvocationHandler {

    private Subject mSubject;

    public DynamicProxy(Subject subject){
        mSubject = subject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(mSubject, args);
    }
}
