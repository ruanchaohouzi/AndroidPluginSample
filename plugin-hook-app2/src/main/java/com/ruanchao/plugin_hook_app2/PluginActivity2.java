package com.ruanchao.plugin_hook_app2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

public class PluginActivity2 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin2);

        /***
         * 一定要注意Context必须要采取Application的Context文件，而不能采取当前Activity的Context文件
         * 由于作为插件使用，packageName必须要采取宿主Context的PackageName,而不是插件自身的包名
         * 否则会出现加载失败
         */

        findViewById(R.id.btn_next_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(getApplication(), NextPageActivity.class));
                getApplication().startActivity(intent);
            }
        });
    }
}
