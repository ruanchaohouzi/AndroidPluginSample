package com.ruanchao.app2;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ruanchao.app2.test.Test2Activity;
import com.ruanchao.app2.utils.PermissionsManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static com.ruanchao.app2.hook.HookAmsUtil.PLUGIN_PATH;

public class MainActivity extends Activity implements View.OnClickListener {

    /**
     * 需要进行检测的权限数组
     */
    public final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private String mActivityName;
    private Class<?> pluginMainClass;
    private PermissionsManager mPermissionsManager;
    private boolean isNeedCheck = true;
    private static final int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsManager = new PermissionsManager(this);
        findViewById(R.id.btn_start_plugin).setOnClickListener(this);
        findViewById(R.id.btn_start_normal).setOnClickListener(this);
        findViewById(R.id.btn_add_plugin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_plugin:
                if (pluginMainClass == null){
                    Toast.makeText(MainActivity.this, "请先加载插件", Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    Intent intent = new Intent(MainActivity.this, pluginMainClass);
                    startActivity(intent);
                }catch (Exception e){
                    Log.i("MainActivity", "err:" + e.getMessage());
                }
                break;
            case R.id.btn_start_normal:
                Intent intent = new Intent(MainActivity.this, Test2Activity.class);
                startActivity(intent);
                break;
            case R.id.btn_add_plugin:
                loadPlugin();
                break;
            default:
                break;
        }
    }

    private void loadPlugin() {
        DexClassLoader dexClassLoader = new DexClassLoader(PLUGIN_PATH,
                getDir("dex",MODE_PRIVATE).getAbsolutePath(),
                null,
                getClassLoader());
        PackageInfo packageArchiveInfo = getPackageManager().getPackageArchiveInfo(PLUGIN_PATH, PackageManager.GET_ACTIVITIES);
        mActivityName = packageArchiveInfo.activities[0].name;
        try {
            pluginMainClass = dexClassLoader.loadClass(mActivityName);
            //----------------接下来，把插件dex文件加载到系统中------------------
            loadDexElements(dexClassLoader);
            //---------------继续，把插件的dex中的Resource文件加载到内存中，此过程要在Application中，
            //               由于Resource和Assest在整个插件中都要使用，要作为全局变量----------------
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDexElements(DexClassLoader dexClassLoader) throws Exception {
        //----------------接下来，把插件dex文件加载到系统中------------------
        //1.获取宿主和插件的字段pathList 对应的对象是DexPathList
        Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object hostPathListValue = pathListField.get(getClassLoader());
        Object pluginPathListValue = pathListField.get(dexClassLoader);
        //2.获取宿主和插件的dexElement
        Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
        Field dexElementField = dexPathListClass.getDeclaredField("dexElements");
        dexElementField.setAccessible(true);
        Object hostDexElementValue = dexElementField.get(hostPathListValue);
        Object pluginDexElementValue = dexElementField.get(pluginPathListValue);
        //3.组合到一起
        Object dexElementsValue = combineArray(hostDexElementValue, pluginDexElementValue);
        //把组合后的dexElementsValue设置到系统中
        dexElementField.set(hostPathListValue, dexElementsValue);
    }


    private Object combineArray(Object hostDexElementValue, Object pluginDexElementValue) {
        //获取原数组类型
        Class<?> localClass = hostDexElementValue.getClass().getComponentType();
        //获取原数组长度
        int i = Array.getLength(hostDexElementValue);
        //插件数组加上原数组的长度
        int j = i + Array.getLength(pluginDexElementValue);
        //创建一个新的数组用来存储
        Object result = Array.newInstance(localClass, j);
        //一个个的将dex文件设置到新数组中
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(hostDexElementValue, k));
            } else {
                Array.set(result, k, Array.get(pluginDexElementValue, k - i));
            }
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
            if (isNeedCheck){
                mPermissionsManager.checkPermissions(PERMISSION_CODE, PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (!mPermissionsManager.verifyPermissions(grantResults)) {
                //mPermissionsManager.showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }
}
