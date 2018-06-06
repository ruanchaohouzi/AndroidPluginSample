package com.ruanchao.app.pluginmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by ruanchao on 2018/1/22.
 */

public class PluginManager {

    private Context mContext;
    private DexClassLoader pluginDexClassLoader;
    private Resources pluginResources;
    private PackageInfo pluginPackageInfo;
    private static PluginManager mPluginManager;

    private PluginManager(){}
    public synchronized static PluginManager getInstance(){
        if (mPluginManager == null){
            mPluginManager = new PluginManager();
        }
        return mPluginManager;
    }

    public void setContext(Context context){
        mContext = context;
    }

    /**
     * 核心是
     * （1）从dex获取插件中Class文件
     *  (2)获取插件资源Resource文件
     * @param dexPath
     */
    public void loadApk(String dexPath){

        //1.获取插件DexClassLoader
        pluginDexClassLoader = new DexClassLoader(dexPath,
                mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(),
                null,
                mContext.getClassLoader());

        //2.获取插件PackageInfo
        pluginPackageInfo = mContext.getPackageManager().getPackageArchiveInfo(dexPath, PackageManager.GET_ACTIVITIES);

        //3.需要实例化Resources
        try {
            //获取插件的AssetsManager
            AssetManager pluginAssetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(pluginAssetManager, dexPath);

            pluginResources = new Resources(pluginAssetManager,
                    mContext.getResources().getDisplayMetrics(),
                    mContext.getResources().getConfiguration());
        }catch (Exception e){

        }

    }

    public DexClassLoader getPluginDexClassLoader(){
        return pluginDexClassLoader;
    }

    public Resources getPluginResources(){
        return pluginResources;
    }

    public PackageInfo getPluginPackageInfo(){
        return pluginPackageInfo;
    }
}
