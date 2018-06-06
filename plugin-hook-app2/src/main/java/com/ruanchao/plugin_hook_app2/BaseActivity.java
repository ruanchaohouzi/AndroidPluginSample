package com.ruanchao.plugin_hook_app2;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Created by ruanchao on 2018/5/31.
 */

public class BaseActivity extends Activity {

    @Override
    public Resources getResources() {
        //决定了资源文件的寻找目录，否则在插件中，宿主就找不到资源文件id
        if(getApplication() != null && getApplication().getResources() != null){
            return getApplication().getResources();
        }
        return super.getResources();
    }


    @Override
    public AssetManager getAssets() {
        if(getApplication() != null && getApplication().getAssets() != null){
            return getApplication().getAssets();
        }
        return super.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        if(getApplication() != null && getApplication().getTheme() != null){
            return getApplication().getTheme();
        }
        return super.getTheme();
    }
}
