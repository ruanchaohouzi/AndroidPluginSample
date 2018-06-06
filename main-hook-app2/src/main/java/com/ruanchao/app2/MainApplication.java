package com.ruanchao.app2;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.ruanchao.app2.hook.HookAmsUtil;

import java.lang.reflect.Method;

/**
 * Created by ruanchao on 2018/5/29.
 */

public class MainApplication extends Application {

    private AssetManager assetManager;
    private Resources newResource;
    private Resources.Theme mTheme;

    @Override
    public void onCreate() {
        super.onCreate();
//        HookAmsUtil hookAmsUtil = new HookAmsUtil(this);
        try {
            //1. Hook IActivityManager方式实现
//            hookAmsUtil.hookIActivityManager();
//            hookAmsUtil.hookActivityThreadHandler();

            //2.Hook Instrumentation方式实现
//            hookAmsUtil.hookInstrumentation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HookAmsUtil hookAmsUtil = new HookAmsUtil(base);
        try {
            //2.Hook Instrumentation方式实现
            hookAmsUtil.hookInstrumentation();
            //必须要加载插件dex文件中的resource资源和Asset资源
            initResource(base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initResource(Context base) {
        try {
            assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager,HookAmsUtil.PLUGIN_PATH);
            Method ensureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocks.setAccessible(true);
            ensureStringBlocks.invoke(assetManager);
            newResource = new Resources(assetManager, getResources().getDisplayMetrics(),
                    getResources().getConfiguration());
            mTheme = newResource.newTheme();
            mTheme.setTo(super.getTheme());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets(): assetManager;
    }

    @Override
    public Resources getResources() {
        return newResource == null ? super.getResources():newResource;
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme(): mTheme;
    }
}
