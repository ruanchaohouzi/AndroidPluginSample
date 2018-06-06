package com.shuwen.pluginapk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.ruanchao.plugin_interface.PluginInterface;

/**
 * Created by ruanchao on 2018/1/22.
 */

public class BaseActivity extends Activity implements PluginInterface {

    public Activity mContext;
    @Override
    public void onCreate(Bundle saveInstance) {

    }

    @Override
    public void attachContext(Activity context) {

        this.mContext = context;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    ///////////关于上下文Context都需要重写，决定采取自身的Context，还是作为插件采取宿主的Context///////////////////////////////////////
    @Override
    public void finish() {
        if (mContext == null){
            super.finish();
        }else {
            mContext.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mContext == null){
            super.onBackPressed();
        }else {
            mContext.onBackPressed();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (mContext == null){
            super.startActivity(intent);
        }else {
            mContext.startActivity(intent);
        }
    }

    @Override
    public void setContentView(View view) {
        if (mContext == null){
            super.setContentView(view);
        }else {
            mContext.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (mContext == null){
            super.setContentView(view, params);
        }else {
            mContext.setContentView(view, params);
        }
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        if (mContext == null){
           return super.getLayoutInflater();
        }else {
            return mContext.getLayoutInflater();
        }
    }

    @Override
    public Window getWindow() {
        if (mContext == null){
            return super.getWindow();
        }else {
            return mContext.getWindow();
        }
    }

    @Override
    public<T extends View> T findViewById(int id) {
        if (mContext == null){
            return super.findViewById(id);
        }else {
            return mContext.findViewById(id);
        }
    }
    @Override
    public ClassLoader getClassLoader() {
        if (mContext == null){
            return super.getClassLoader();
        }else {
            return mContext.getClassLoader();
        }
    }

    @Override
    public WindowManager getWindowManager() {
        if (mContext == null){
            return mContext.getWindowManager();
        }else {
            return mContext.getWindowManager();
        }
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        if (mContext == null){
            return super.getApplicationInfo();
        }else {
            return mContext.getApplicationInfo();
        }
    }

}
