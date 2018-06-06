### 一、Android插件化开发，常见有三种实现方式：

（1）Android 动态加载dex文件
（2）Android动态加载APK文件（代理模式）
（3）Hook技术动态加载APK文件中的Activity

### 二、Android 动态加载dex文件
1.反射方式加载（较麻烦，不介绍），需要反射出具体的方法等

2.接口编程的方式加载（以下重点介绍），只需要反射出具体的类即可，转换成接口对象操作具体的方法，相对简单。

（1）新建接口

（2）接口的实现类

（3）对接口实现类转化成Dex文件

（4）加载具体位置的dex文件
```
//下面开始加载dex class
DexClassLoader dexClassLoader = new DexClassLoader(internalPath, cacheFile.getAbsolutePath(), null, getClassLoader());
try {
    Class libClazz = dexClassLoader.loadClass("com.shuwen.dynamicdex.dynamic.impl.DynamicImpl");
    dynamic = (Dynamic) libClazz.newInstance();
    if (dynamic != null)
        Toast.makeText(this, dynamic.sayHello(), Toast.LENGTH_LONG).show();
} catch (Exception e) {
    e.printStackTrace();
}
```

### 三、Android动态加载APK文件（代理模式）
#### 1.新建库plugin-interface，在该库中新建接口PluginInterface
主要为了确保宿主APP和插件APP中接口的一致性。接口PluginInterface，这套标准用来规范宿主与Plugin之间的上下文以及生命周期关系的标准
```
public interface PluginInterface {
 void onCreate(Bundle saveInstance);
void attachContext(FragmentActivity context);
void onStart(); void onResume();
void onRestart();
void onDestroy();
void onStop();
 void onPause();
}
```

#### 2.PluginManager  宿主需要一套工具，工具主要实现三个功能
（1）获取插件中的DexClassLoader

（2）获取插件的PackageInfo，主要为了获取插件中的activity信息等。

（3）获取插件的AssetsManager，主要为了获取插件中的资源信息。

```
public void loadApk(String dexPath){

    //1.获取插件DexClassLoader
    pluginDexClassLoader = new DexClassLoader(dexPath,
            mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(),
            null,
            mContext.getClassLoader());

    //2.获取插件PackageInfo
    pluginPackageInfo = mContext.getPackageManager().getPackageArchiveInfo(dexPath, PackageManager.GET_ACTIVITIES);

    //3  .获取插件的AssetsManager
    try {
        AssetManager pluginAssetManager = AssetManager.class.newInstance();
        Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
        addAssetPath.invoke(pluginAssetManager, dexPath);
        pluginResources = new Resources(pluginAssetManager,
                mContext.getResources().getDisplayMetrics(),
                mContext.getResources().getConfiguration());
    }catch (Exception e){
    }

}
```

#### 3.ProxyActivity是宿主的Activity，这个ProxyActivity只是一个空壳，提供一套生命周期和上下文给我们自己创建的PluginActivity的的实例用的。必须要把宿主中的Context信息传入到插件Activity中。

```
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
```
4.插件activity 需要实现接口PluginInterface，需要传入数组Activity中的context

```
public class BaseActivity extends Activity implements PluginInterface {

    private Activity mContext;
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

    @Override
    public void finish() {
        mContext.finish();
    }

    @Override
    public void onBackPressed() {
        mContext.onBackPressed();
    }

    @Override
    public void startActivity(Intent intent) {
        mContext.startActivity(intent);
    }

    @Override
    public void setContentView(View view) {
        mContext.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mContext.setContentView(view, params);
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return mContext.getLayoutInflater();
    }

    @Override
    public Window getWindow() {
        return mContext.getWindow();
    }

    @Override
    public View findViewById(int id) {
        return mContext.findViewById(id);
    }
    @Override
    public ClassLoader getClassLoader() {
        return mContext.getClassLoader();
    }

    @Override
    public WindowManager getWindowManager() {
        return mContext.getWindowManager();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mContext.getApplicationInfo();
    }

}
```

### 三、Android动态加载APK文件（hook技术）

#### 一、Activity的启动流程

##### （1）根Activity的启动流程
首先Launcher进程向AMS请求创建根Activity，AMS会判断根Activity所需的应用程序进程是否存在并启动，如果不存在就会请求Zygote进程创建应用程序进程。应用程序进程启动后，AMS会请求应用程序进程创建并启动根Activity。
#### （2）普通Activity的启动流程
应用程序进程中的Activity向AMS请求创建普通Activity（步骤1）主要通过IActivityManager进行通信，AMS会对这个Activty的生命周期管和栈进行管理，校验Activity等等。如果Activity满足AMS的校验，AMS就会请求应用程序进程中的ActivityThread去创建并启动普通Activity（步骤2）。
#### 二、Hook方案的实现——Hook  IActivityManager
通过Activity的启动流程，Hook点只有两点IActivityManager和ActivityThread。因为AMS是系统进程，我们无法修改，只能在应用程序修改内存。
（1）IActivityManager借助了Singleton类来实现单例，而且gDefault又是静态的，因此IActivityManager是一个比较好的Hook点。
（2）ActivityThread也是单例的，借助静态sCurrentActivityThread对象 进行Hook

实现思路：（1）用代理注册的Activity代替插件Activity，主要是为了让未注册的Activity通过AMS的校验。
                  （2）从代理Activity还原插件Activity，实现插件Activity的显示。

* 用代理注册的Activity代替插件Activity，主要是为了让未注册的Activity通过AMS的校验。
1、Activity要想启动必须要在清单文件注册通过AMS校验，为了通过校验采取注册的代理Activity占坑。该代理Activity只起到占坑的目的
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.ruanchao.app2">
    <application>
       //………...
        <activity android:name=".hook.ProxyActivity" />
       //…...
    </application>

</manifest>
```
所以思路是：先用占坑的Activity代理插件的Activity骗过AMS 校验，然后校验通过后，再用插件Activity代理取代占坑的Activity。

2、Activity启动源码分析——请求AMS的流程
（1）Activity通过Instrumentation启动
```
//Activity中
public void startActivityForResultAsUser(Intent intent, String resultWho, int requestCode,
        @Nullable Bundle options, UserHandle user) {
     //…………….
    Instrumentation.ActivityResult ar = mInstrumentation.execStartActivity(
            this, mMainThread.getApplicationThread(), mToken, resultWho, intent, requestCode,
            options, user);
     //………….
}
```
(2）Instrumentation最终通过ActivityManager.getService()来启动Activity，此方法的返回值是IActivityManager接口AIDL文件，与AMS进行通信的桥梁。
```
//Instrumentation中
//Android8.0以上版本
public ActivityResult execStartActivity(
 //………..
        int result = ActivityManager.getService()
            .startActivity(whoThread, who.getBasePackageName(), intent,
                    intent.resolveTypeIfNeeded(who.getContentResolver()),
                    token, target != null ? target.mEmbeddedID : null,
                    requestCode, 0, null, options);
//………….
}
//Android8.0以下版本
public ActivityResult execStartActivity(
 //………..
        int result = ActivityManagerNative.gDefault()
            .startActivity(whoThread, who.getBasePackageName(), intent,
                    intent.resolveTypeIfNeeded(who.getContentResolver()),
                    token, target != null ? target.mEmbeddedID : null,
                    requestCode, 0, null, options);
//………….
}
```
（3）ActivityManager中,getService()的返回值最终是IActivityManager
Android8.0以下版本
```
static public IActivityManager getDefault() {
       return gDefault.get();
   }
private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
       protected IActivityManager create() {
           IBinder b = ServiceManager.getService("activity");
           if (false) {
               Log.v("ActivityManager", "default service binder = " + b);
           }
           IActivityManager am = asInterface(b);
           if (false) {
               Log.v("ActivityManager", "default service = " + am);
           }
           return am;
       }
   };
```

Android8.0以上版本
```
//ActivityManager
public static IActivityManager getService() {
    return IActivityManagerSingleton.get();
}
//IActivityManagerSingleton是一个单例模式
private static final Singleton<IActivityManager> IActivityManagerSingleton =
        new Singleton<IActivityManager>() {
            @Override
            protected IActivityManager create() {
                final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                final IActivityManager am = IActivityManager.Stub.asInterface(b);
                return am;
            }
        };

//Singleton
public abstract class Singleton<T> {
    private T mInstance;
    protected abstract T create();
    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}
```

（4）从源代码分析可知IActivityManager是单例模式，在内存中只有一份，所有可以通过Hook技术修改内存中的IActivityManager的值
Hook技术的核心是动态代理和反射
```
public void hookIActivityManager() throws Exception {
    Log.i(TAG, "start hookIActivityManager");
    Class<?> activityManagerNativeClass;
    Field gDefaultFile;
    /**
     * 核心
     * 由于IActivityManagerSingleton是单例模式，可以拿到系统该单例对象并且修改该对象
     * 只有系统单例的对象修改才有效果
     */
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
        //反射获取类
        activityManagerNativeClass = Class.forName("android.app.ActivityManager");
        //获取类中的字段
        gDefaultFile = activityManagerNativeClass.getDeclaredField("IActivityManagerSingleton");
    }else {
        //反射获取类
        activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
        //获取类中的字段
        gDefaultFile = activityManagerNativeClass.getDeclaredField("gDefault");
    }
    //设置字段可访问
    gDefaultFile.setAccessible(true);
    //获取反射字段的值，静态方法，不需要传入对象，所以对象为null
    Object gDefaultFileValue = gDefaultFile.get(null);
    //获取gDefault.get()的值，主要在Singleton中
    Class<?> singletonClass = Class.forName("android.util.Singleton");
    Field mInstanceFile = singletonClass.getDeclaredField("mInstance");
    mInstanceFile.setAccessible(true);
    //非静态方法，需要传入对象,获取系统的IActivityManager
    Object IActivityManager = mInstanceFile.get(gDefaultFileValue);
    //获取IActivityManager接口

    Class<?> IActivityManagerClass = Class.forName("android.app.IActivityManager");
    //接下来需要创建钩子，替换系统的IActivityManager，主要采取动态代理的技术构造IActivityManager
    ProxyIActivityManager proxyIActivityManager= new ProxyIActivityManager(IActivityManager);
    Object proxy = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{IActivityManagerClass},
            proxyIActivityManager);
    //hook 就是为了替换IActivityManager的值,以下就是替换操作
    mInstanceFile.set(gDefaultFileValue, proxy);
    /////////到这里为止，已经实现了用代理Activity来替换未注册的Activity，通过PackageManagerService校验////////////
    //接下来找到系统的ActivityThread 并且要找到单例对象，才可以修改该对象值
}
//动态代理，在执行方法之前，先执行代理里面的方法
class ProxyIActivityManager implements InvocationHandler{

    private Object iActivityManager;

    public ProxyIActivityManager(Object iActivityManager){
        this.iActivityManager = iActivityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.i(TAG, "ProxyIActivityManager invoke:" + method.getName());
        if (method.getName().contains("startActivity")){

            int index = 0;
            Intent realIntent = null;
            for (int i = 0; i<args.length; i++){
                if (args[i] instanceof Intent){
                    realIntent = (Intent) args[i];//真正的Intent，无法通过PackageManagerService检查
                    index = i;
                    break;
                }
            }
            //代理Intent,可以通过PackageManagerService检查
            Intent proxyIntent = new Intent(mContext, ProxyActivity.class);
            proxyIntent.putExtra(REAL_INTENT, realIntent);
            args[index] = proxyIntent;
        }
        return method.invoke(iActivityManager, args);
    }
}
```

* 从代理Activity还原插件Activity，实现插件Activity的显示。

（1）Activity通过AMS校验后，AMS通过调用ActivityThread启动Activity的流程。
ActivityThread会通过H类代码的逻辑切换到主线程中，H类是ActivityThread的内部类并继承自Handler，如下所示
```
private class H extends Handler {
    public void handleMessage(Message msg) {
        if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
        switch (msg.what) {
            case LAUNCH_ACTIVITY: {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
                r.loadedApk = getLoadedApkNoCheck(
                        r.activityInfo.applicationInfo, r.compatInfo);
                handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }
            break;
            case RELAUNCH_ACTIVITY: {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityRestart");
                ActivityClientRecord r = (ActivityClientRecord) msg.obj;
                handleRelaunchActivity(r);
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }
            break;
//…………………………..
        }
    }
}
```

```
//ActivityThread的实例对象获取，其实就是反射获取静态的sCurrentActivityThread

public static ActivityThread currentActivityThread() {

return sCurrentActivityThread;
}
```

（2）Handler消息分发处理源码
Handler会首先分发给msg自身的callback处理，如果自身Msg没有msg，会分发到Handler的mCallback处理，最后会分发到回调中的HandleMessage
所以Hook一个mCallback执行替换Intent操作，最后处理HandleMessage.

```
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```

（3）ActivityThread Hook实现
在ActivityThread中找到Handler，然后再hook Handler中的callback
```
public void hookActivityThreadHandler() throws Exception {
    Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
    Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
    currentActivityThreadField.setAccessible(true);
    Object currentActivityThreadValue = currentActivityThreadField.get(null);
    Field mHandlerField = activityThreadClass.getDeclaredField("mH");
    mHandlerField.setAccessible(true);
    Handler handlerValue = (Handler) mHandlerField.get(currentActivityThreadValue);
    Field mCallbackField = Handler.class.getDeclaredField("mCallback");
    mCallbackField.setAccessible(true);
    mCallbackField.set(handlerValue, new HandlerCallback(handlerValue));
}

class HandlerCallback implements Handler.Callback{

    private Handler mHandler;
    public HandlerCallback(Handler handlerValue){
        mHandler = handlerValue;
    }

    @Override
    public boolean handleMessage(Message msg) {
        //LAUNCH_ACTIVITY 的what值是100
        if (msg.what == 100){
            //先处理自己的Handler消息，再处理ActivityThread中自身的handler消息
            try {
                Log.i(TAG,"LAUNCH_ACTIVITY");
                Object activityClientRecord = msg.obj;//ActivityClientRecord
                Field intentField = activityClientRecord.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent proxyIntent = (Intent) intentField.get(activityClientRecord);
                Intent realIntent = (Intent) proxyIntent.getParcelableExtra(REAL_INTENT);
                if (realIntent != null){
                    //方法一，直接替换intent
                    //intentField.set(activityClientRecord, realIntent);
                    //方法二 替换component
                    proxyIntent.setComponent(realIntent.getComponent());
                }
            }catch (Exception e){
            }
        }
        //处理ActivityThread中自身的handler消息
        mHandler.handleMessage(msg);
        return true;
    }
}
```

二、Hook方案的实现2——Hook  Instrument
1、源码分析
（1）Activity中通过Instrumentation启动Activity到AMS

```
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
                                   @Nullable Bundle options) {
    //............
    Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);
    //...............
}
```
（2）ActivityThread启动activity流程
Activity通过Handler处理消息LAUNCH_ACTIVITY，handlerMessage中调取handleLaunchActivity
```
private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {

    //............
    Activity a = performLaunchActivity(r, customIntent);
    //........
}
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    //.............
    Activity activity = mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
    //..............
}
```

从这两个流程可以看出，Activity启动到AMS校验的过程和AMS通过ActivityThread启动Acitivity的流程都是通过Instrumentation.
ActivityThread Handler调用了mInstrumentation的newActivity方法，其内部会用类加载器来创建Activity的实例。看到可以得到方案，就是在Instrumentation的execStartActivity方法中用占坑代理Activity来通过AMS的验证，在Instrumentation的newActivity方法中还原TargetActivity，这两部操作都和Instrumentation有关，因此我们可以用自定义的Instrumentation来替换掉mInstrumentation。首先我们自定义一个Instrumentation，在execStartActivity方法中将启动的TargetActivity替换为SubActivity，

2、Hook Instrumentation实现方案
原理：利用Hook技术自己实现Instrumentation，来取代系统的Instrumentation，然后重写execStartActivity方法和newActivity方法。
由于execStartActivity是隐藏方法，只能通过反射调用父类方法
（1）自定义Instrumentation的实现
```
public class HookInstrumentationProxy extends Instrumentation {
    private PackageManager mPackageManager;
    private Instrumentation mInstrumentation;
    private Context mContext;
    public static final String REAL_INTENT  = "realIntent";
    public static final String TAG = HookInstrumentationProxy.class.getSimpleName();

    public HookInstrumentationProxy(Context context, PackageManager packageManager,
                                    Instrumentation instrumentation){
        mPackageManager = packageManager;
        mInstrumentation = instrumentation;
        mContext = context;
    }

    //Activity 到 AMS 的过程
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        Intent intentProxy = intent;
        Log.i(TAG,"HookInstrumentationProxy execStartActivity");
        //清单中找不到当前的Activity，表示未注册，需要替换为代理的Activity
        if (resolveInfos != null && resolveInfos.size() == 0){
            intentProxy = new Intent();
            intentProxy.setComponent(new ComponentName(mContext,ProxyActivity.class));
            intentProxy.putExtra(REAL_INTENT, intent);
            Log.i(TAG,"intentProxy  replace intent");
        }
        try {
            //由于隐藏接口，反射调用父类的方法
            Class<?> instrumentationClass = Class.forName("android.app.Instrumentation");
            Method execStartActivityMethod = instrumentationClass.getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivityMethod.setAccessible(true);
            ActivityResult activityResult = (ActivityResult) execStartActivityMethod.invoke(mInstrumentation, who, contextThread, token, target,
                    intentProxy, requestCode, options);
            Log.i(TAG,"instrumentation execStartActivityMethod");
            return activityResult;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //ActivityThread到handler启动Activity过程
    @Override
    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        //取出替换过的Activity
        if (intent.getParcelableExtra(REAL_INTENT) != null){
            intent = intent.getParcelableExtra(REAL_INTENT);
            className = intent.getComponent().getClassName();
        }
        return (Activity)cl.loadClass(className).newInstance();
    }
}
```

（3）替换系统的Instrumentation

* 问题的关键是怎么找到系统的Instrumentation
* 之前说过ActivityThread是一个单例模式，其中就包含了Instrumentation
* 所以只需要要替换掉ActivityThread中的Instrumentation即可
* 在哪里获取到ActivityThread?这里需要在ContextImpl中获取，以为Context实现对象我是是知道了，所以很容易通过我们自己的Context拿到ActivityThread

```
public void hookInstrumentation(){
        try {
            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            //mMainThread就是一个ActivityThread
            Field activityThreadField = contextImplClass.getDeclaredField("mMainThread");
            activityThreadField.setAccessible(true);
            Object activityThreadValue = activityThreadField.get(mContext);
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentationValue = (Instrumentation) mInstrumentationField.get(activityThreadValue);

            //替换系统的mInstrumentation
            HookInstrumentationProxy instrumentationProxy = new HookInstrumentationProxy(mContext,mContext.getPackageManager(),mInstrumentationValue);
            mInstrumentationField.set(activityThreadValue,instrumentationProxy);
        }catch (Exception e){
            Log.i(TAG, "err:" + e.getMessage());
        }
    }
```

此方法必须要在Application的attachBaseContext中调用，因为Context用到了ContextImpl。
```
Application

@Override
protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    HookAmsUtil hookAmsUtil = new HookAmsUtil(base);
    try {
        //Hook Instrumentation方式实现
        hookAmsUtil.hookInstrumentation();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

注：我们实际已经偷梁换柱成功，可是在使用AppCompatActivity时，它又去向PackageManger去检测父类Activity，没找到。那怎么办，我们继续hook！这里需要Hook的是IPackageManager， 详见https://www.jianshu.com/p/29a1df4f4824

三、动态加载SD卡未安装的插件App

1、动态加载插件dex文件到内存（类似于热修复的原理），就是把插件dex文件加载到系统的DexElement数组中
此过程有点耗时，不建议在Application加载，建议采取手动加载插件的过程加载

```
private void loadPlugin() {
    DexClassLoader dexClassLoader = new DexClassLoader(PLUGIN_PATH,
            getDir("dex",MODE_PRIVATE).getAbsolutePath(),
            null,
            getClassLoader());
    //这是为了获取插件启动的Activity
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
```

2、加载插件资源文件
在Application中加载插件中的资源文件，并向外暴露资源文件引用。
（1）插件Activity中必须采取暴露的资源文件引用，而不能采取自身的资源文件，否则会出现加载不到
（2）插件Activity中的Context必须采取系统Application中的Context，而不是插件自身的Context，类似于代理的方式加载插件

```
//加载资源文件
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
//暴露资源文件
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
```

3.插件Activity的资源引用
（1）可以在BaseActivity中获取Application中的资源引用
```
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
```

（2）具体Activity
```
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
```
