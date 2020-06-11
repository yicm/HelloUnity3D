package com.sensetime.player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unity3d.player.UnityPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";

    @BindView(R.id.textView)
    TextView tv_test;

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
//    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
//    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
//    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
//    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
