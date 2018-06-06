package com.ruanchao.hookdemo.hook;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Created by ruanchao on 2018/6/1.
 */

public class HookUtils {

    public static void hookOnclick(Context context, View view) {
        /**
         * 源码分析：
         * 由于在mOnClickListener 在View类中的mListenerInfo对象中保存
         * （1）获取由于在mOnClickListener的mListenerInfo对象
         * （2）对mOnClickListener中的mListenerInfo对象重新设置成自己Hook的监听器
         */
        try {
            Field mListenerInfoField = View.class.getDeclaredField("mListenerInfo");
            mListenerInfoField.setAccessible(true);
            Object mListenerInfoValue = mListenerInfoField.get(view);
            Class<?> listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
            Field mOnClickListenerField = listenerInfoClass.getDeclaredField("mOnClickListener");
            mOnClickListenerField.setAccessible(true);
            Object mOnClickListenerValue = mOnClickListenerField.get(mListenerInfoValue);
            mOnClickListenerField.set(mListenerInfoValue, new HookOnClickListener(context,(View.OnClickListener) mOnClickListenerValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class HookOnClickListener implements View.OnClickListener {
        View.OnClickListener onClickListener;
        Context context;
        public HookOnClickListener(Context context, View.OnClickListener v) {
            onClickListener = v;
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            Toast.makeText(context,"哈哈，我干坏事了。。。",Toast.LENGTH_LONG).show();
            if (onClickListener != null){
                onClickListener.onClick(v);
            }
        }
    }


}
