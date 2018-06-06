package com.ruanchao.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ruanchao.app.pluginmanager.PluginManager;
import com.ruanchao.app.pluginmanager.ProxyActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mStartPluginApk = (Button) findViewById(R.id.btn_start_plugin_apk);
        mStartPluginApk.setOnClickListener(this);
        Button mLoadPlugun = (Button) findViewById(R.id.load_plugin);
        mLoadPlugun.setOnClickListener(this);

    }

    private void initData() {
        PluginManager.getInstance().setContext(this);
        PluginManager.getInstance().loadApk(Environment.getExternalStorageDirectory().getAbsolutePath() + "/plugin-app-debug.apk");
    }

    private void startApk() {
        Intent intent = new Intent(this, ProxyActivity.class);
        String otherApkMainActivityName = PluginManager.getInstance().getPluginPackageInfo().activities[0].name;
        intent.putExtra("className", otherApkMainActivityName);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_plugin_apk:
                startApk();
                break;
            case R.id.load_plugin:
                initData();
                break;
                default:
                    break;
        }
    }
}
