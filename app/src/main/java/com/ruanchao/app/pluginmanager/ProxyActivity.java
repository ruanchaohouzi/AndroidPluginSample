package com.ruanchao.app.pluginmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ruanchao.plugin_interface.PluginInterface;

/**
 * Created by ruanchao on 2018/1/22.
 */

public class ProxyActivity extends Activity {

    private PluginInterface pluginInterface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String className = getIntent().getStringExtra("className");
        try {
            Class<?> aClass = PluginManager.getInstance().getPluginDexClassLoader().loadClass(className);
            Object newInstance = aClass.newInstance();
            if (newInstance instanceof PluginInterface){
                pluginInterface = (PluginInterface) newInstance;
                pluginInterface.attachContext(this);
                pluginInterface.onCreate(new Bundle());
            }

        } catch (ClassNotFoundException e) {

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        return PluginManager.getInstance().getPluginResources();
    }

    @Override
    protected void onStart() {
        pluginInterface.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        pluginInterface.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        pluginInterface.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        pluginInterface.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        pluginInterface.onRestart();
        super.onRestart();
    }

    @Override
    public void startActivity(Intent intent) {
        Intent newIntent = new Intent(this, ProxyActivity.class);
        newIntent.putExtra("className", intent.getComponent().getClassName());
        super.startActivity(newIntent);
    }

}
