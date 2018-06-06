package com.ruanchao.plugin_interface;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ruanchao on 2018/1/22.
 */

public interface PluginInterface {

    /**
     * 生命周期
     * @param saveInstance
     */
    void onCreate(Bundle saveInstance);
    void onStart();
    void onResume();
    void onRestart();
    void onDestroy();
    void onStop();
    void onPause();


    /**
     * 获取宿主的上下文
     */
    void attachContext(Activity context);
}
