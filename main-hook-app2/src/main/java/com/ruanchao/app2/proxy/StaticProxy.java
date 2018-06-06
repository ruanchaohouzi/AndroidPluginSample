package com.ruanchao.app2.proxy;

/**
 * Created by ruanchao on 2018/5/28.
 */

public class StaticProxy implements Subject {

    private Subject mSubject;

    public StaticProxy(Subject subject){
        mSubject = subject;
    }

    @Override
    public String buy() {
        return mSubject.buy();
    }
}
