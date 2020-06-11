[TOC]

**开发环境**：

- Unity 2019.3.14f1
- Android Studio 3.4.1
    - Android9.+(Q)
    - 28.0.3 Build Tools

## 1 Unity导出Android工程

![](https://www.xiaobaiai.net/imgs/20200611201018.png)

设置场景背景透明：

背景色RGBA设置为0，0，0，0。

![](https://www.xiaobaiai.net/imgs/20200611201030.png)

## 2 Unity场景显示为透明子视图

### 2.1 默认导出工程

1. 默认导出的 Android Project 包含 `launcher` 和 `unityLibrary` 两个模块：

![](https://www.xiaobaiai.net/imgs/20200611201036.png)

2. 默认导出的 Android Project 启动窗口为 `unityLibrary` 模块中的 `UnityPlayerActivity`：

![](https://www.xiaobaiai.net/imgs/20200611201042.png)

### 2.2 将导出的Unity场景作为子模块添加到其他工程中

1. 新建一个 `Empty Activity` 项目（包名随意命名，无需跟unityLibrary包名一致）
2. 我们将 `activity_main.xml` 设计成这样：

![](https://www.xiaobaiai.net/imgs/20200611201048.png)

3. 导入 `unityLibrary` 模块到工程中
4. app和unityLibrary模块设置
    - 设置依赖 `unityLibrary` 的lib目录aar文件，到工程的 `build.gradle`:
    ```gradle
    allprojects {
        repositories {
            google()
            jcenter()
            // here
            flatDir {
                dirs "${project(':unityLibrary').projectDir}/libs"
            }
        }
    }
    ```
    - 将 `unityLibrary` 设置为默认 `app` 模块的依赖模块，到 `app` 的`build.gradle`:
    ```gradle
    dependencies {
        // ......
        // here
        implementation project(path: ':unityLibrary')
    }
    ```
    - 此时会出现合并`AndroidManifest.xml` 主题(Theme)错误，因为两个模块使用了不同的主题配置，将`unityLibrary`的 `AndroidManifest.xml` `<application` 中 `android:theme="@style/UnityThemeSelector.Translucent"` 去掉即可。
    - 同时我们需要将 `unityLibrary` 的默认启动 activity `UnityPlayerActivity` 配置注释掉：
    ```xml
    <application android:isGame="true">
    <activity android:name="com.unity3d.player.UnityPlayerActivity" android:theme="@style/UnityThemeSelector" android:screenOrientation="fullSensor" android:launchMode="singleTask" android:configChanges="mcc|mnc|locale|touchscreen|keyboard|keyboardHidden|navigation|orientation|screenLayout|uiMode|screenSize|smallestScreenSize|fontScale|layoutDirection|density" android:hardwareAccelerated="false">
      <!-- 注释掉 -->
      <!--<intent-filter>-->
        <!--<action android:name="android.intent.action.MAIN" />-->
        <!--<category android:name="android.intent.category.LAUNCHER" />-->
        <!--<category android:name="android.intent.category.LEANBACK_LAUNCHER" />-->
      <!--</intent-filter>-->
      <meta-data android:name="unityplayer.UnityActivity" android:value="true" />
    </activity>
    ```
    - 为了能够调用到 `unityLibrary` 中的jar文件，即复用另一个模块的jar，需要修改 `unityLibrary` 中的`build.gradle`：
    ```gradle
    dependencies {
        // 将implementation修改为api
        api fileTree(dir: 'libs', include: ['*.jar'])
        implementation(name: 'UnityAds', ext:'aar')
        implementation(name: 'UnityAdsAndroidPlugin', ext:'aar')
    }
    ```
5. 显示为子视图应用实现
    - app 模块添加 `butterknife` 资源绑定组件，简化我们编码，即app `build.gradle`:
    ```gradle
    android {
        compileSdkVersion 29
        // ButterKnife需要开启Java8哦
        // Butterknife requires Java 8.
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
        // ......
    }
    dependencies {
        // ......

        implementation project(path: ':unityLibrary')

        // here
        // 记得用10.2.1哦，不要用8.x，会跟AndroidX不适配
        implementation 'com.jakewharton:butterknife:10.2.1'
        annotationProcessor 'com.jakewharton:butterknife-compiler:10.2.1'
    }
    ```
    - 到`res/values/strings.xml`添加一个资源描述，否则后面会出现找不到资源直接甭掉：
    ```xml
    <resources>
        <string name="app_name">xxx</string>
        <!-- here -->
        <string name="game_view_content_description">Game view</string>
    </resources>
    ```
    - 到 app 模块 `MainActivity.java` 中添加应用代码，注意`onResume`和`onConfigurationChanged`要重载：
    ```java
    public class MainActivity extends AppCompatActivity {
        public final static String TAG = "MainActivity";

        @BindView(R.id.textView) TextView tv_test;

        private Unbinder mUnbinder;
        private UnityPlayer mUnityPlayer;
        private View mPlayerView;
        private LinearLayout mPlayerLayout;
        LinearLayout.LayoutParams mPlayerLayoutParams;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mUnbinder = ButterKnife.bind(this);

            // Create the UnityPlayer
            mUnityPlayer = new UnityPlayer(this);

            // Transparent background
            if (mUnityPlayer.getChildCount() > 0 && mUnityPlayer.getChildAt(0) instanceof SurfaceView) {
                SurfaceView surfaceView = ((SurfaceView) mUnityPlayer.getChildAt(0));
                surfaceView.setZOrderOnTop(true);
                surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            }

            mPlayerView = mUnityPlayer.getView();

            // Add the Unity view SLIGHTY MODIFIED
            mPlayerLayout = (LinearLayout)findViewById(R.id.player_linear_layout);
            mPlayerLayoutParams = (LinearLayout.LayoutParams) mPlayerLayout.getLayoutParams();

            mPlayerLayout.addView(mPlayerView);
            mUnityPlayer.requestFocus();
        }

        @OnClick({R.id.player, R.id.launcher})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.player:
                    tv_test.setText("Player");
                    UnityPlayer.UnitySendMessage("Player", "Jump", "");
                    break;
                case R.id.launcher:
                    tv_test.setText("Launcher");
                    UnityPlayer.UnitySendMessage("Launcher", "CreateBallPrefab", "");
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mUnbinder != null) {
                mUnbinder.unbind();
            }
        }

        // Pause Unity
        @Override protected void onPause()
        {
            super.onPause();
            mUnityPlayer.pause();
        }

        // Resume Unity
        @Override protected void onResume()
        {
            super.onResume();
            // step1
            mUnityPlayer.resume();
        }

        // Low Memory Unity
        @Override public void onLowMemory()
        {
            super.onLowMemory();
            mUnityPlayer.lowMemory();
        }

        // Trim Memory Unity
        @Override public void onTrimMemory(int level)
        {
            super.onTrimMemory(level);
            if (level == TRIM_MEMORY_RUNNING_CRITICAL)
            {
                mUnityPlayer.lowMemory();
            }
        }

        // This ensures the layout will be correct.
        @Override public void onConfigurationChanged(Configuration newConfig)
        {
            super.onConfigurationChanged(newConfig);
            // step2
            mUnityPlayer.configurationChanged(newConfig);
        }

        // Notify Unity of the focus change.
        @Override public void onWindowFocusChanged(boolean hasFocus)
        {
            super.onWindowFocusChanged(hasFocus);
            mUnityPlayer.windowFocusChanged(hasFocus);
        }

        // For some reason the multiple keyevent type is not supported by the ndk.
        // Force event injection by overriding dispatchKeyEvent().
        @Override public boolean dispatchKeyEvent(KeyEvent event)
        {
            if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
                return mUnityPlayer.injectEvent(event);
            return super.dispatchKeyEvent(event);
        }

        // Pass any events not handled by (unfocused) views straight to UnityPlayer
        @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
        @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
        @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
        /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
    }
    ```
6. 最后结果
    ![](https://www.xiaobaiai.net/imgs/20200611201056.png)


## 3 代码分析

我们在Unity工程中的Game Object 脚本中加了两个自己定义的成员函数：

```c#
public class Launcher : MonoBehaviour
{
    public GameObject ballPrefab;
    // Use this for initialization
    void Start() { // ...... }

    // Update is called once per frame
    void Update() { // ...... }

    // 自定义成员函数：创建小球
    void CreateBallPrefab()
    {
        Instantiate(this.ballPrefab);
    }
}
```

```c#
public class Player : MonoBehaviour
{
    protected float jump_speed = 5.0f;

    // Start is called before the first frame update
    void Start() { // ...... }

    // Update is called once per frame
    void Update() { // ...... }

    // 自定义成员函数
    void Jump()
    {
        this.GetComponent<Rigidbody>().velocity = Vector3.up * this.jump_speed;
    }
}
```

导出Android Project后，可以通过下面方式调用：

`UnityPlayer.UnitySendMessage("GameObjectName", "MethodName", "parameter to send");`

我们Android工程中通过使用两个按钮触发点击事件调用下面两个动作，从而操作Unity场景中的物体：

```java
UnityPlayer.UnitySendMessage("Player", "Jump", "");
UnityPlayer.UnitySendMessage("Launcher", "CreateBallPrefab", "");
```

## 4 Github代码

[https://github.com/yicm](https://github.com/yicm)


## 5 参考链接

- https://forum.unity.com/threads/using-unity-android-in-a-sub-view.98315/
- https://forum.unity.com/threads/unity3d-export-to-android-with-transparent-background.512129/
- https://blog.csdn.net/s15100007883/article/details/103414879
- https://stackoverflow.com/questions/5391089/how-to-make-surfaceview-transparent
- https://www.cnblogs.com/OctoptusLian/p/8418534.html
- https://www.cnblogs.com/OctoptusLian/p/8529313.html
