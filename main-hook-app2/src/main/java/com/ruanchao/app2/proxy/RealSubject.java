package com.ruanchao.app2.proxy;

/**
 * Created by ruanchao on 2018/5/28.
 */

public class RealSubject implements Subject {
    @Override
    public String buy() {
        return "RealSubject buy";
    }
}
