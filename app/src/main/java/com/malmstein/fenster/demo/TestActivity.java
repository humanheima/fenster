package com.malmstein.fenster.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.malmstein.fenster.controller.FensterPlayerControllerVisibilityListener;
import com.malmstein.fenster.controller.SimpleMediaFensterPlayerController;
import com.malmstein.fenster.helper.ScreenResolution;
import com.malmstein.fenster.view.CopyFensterVideoView;

public class TestActivity extends Activity implements FensterPlayerControllerVisibilityListener {

    private static final String TAG = "TestActivity";

    private CopyFensterVideoView textureView;
    private SimpleMediaFensterPlayerController fullScreenMediaPlayerController;
    private RelativeLayout rlVideoView;
    private int portraitWidth;
    private boolean isLandscape;

    @Override
    public void onControlsVisibilityChange(boolean value) {
        setSystemUiVisibility(value);
    }

    public static void launch(Context context) {
        Intent starter = new Intent(context, TestActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        bindViews();
        initVideo();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "newConfig.orientation=" + newConfig.orientation);
        if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) {
            isLandscape = true;
            //水平方向
            setLandscape();
        } else {
            isLandscape = false;
            setPortrait();
        }
    }

    @Override
    public void onBackPressed() {
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setFullSensor();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textureView.stopPlayback();
    }

    private void setPortrait() {
        rlVideoView.post(new Runnable() {
            @Override
            public void run() {
                if (portraitWidth == 0) {
                    portraitWidth = ScreenResolution.getResolution(TestActivity.this.getApplicationContext()).first;
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlVideoView.getLayoutParams();
                params.height = portraitWidth / 16 * 9;
                rlVideoView.setLayoutParams(params);
            }
        });
    }

    private void setLandscape() {
        rlVideoView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlVideoView.getLayoutParams();
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                rlVideoView.setLayoutParams(params);
            }
        });
    }

    private void setFullSensor() {
        rlVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }, 2000);
    }

    private void bindViews() {
        rlVideoView = (RelativeLayout) findViewById(R.id.rl_video_view);
        textureView = (CopyFensterVideoView) findViewById(R.id.play_video_texture);
        fullScreenMediaPlayerController = (SimpleMediaFensterPlayerController) findViewById(R.id.play_video_controller);
        setPortrait();
    }

    private void initVideo() {
        fullScreenMediaPlayerController.setVisibilityListener(this);
        textureView.setMediaController(fullScreenMediaPlayerController);
        textureView.setOnPlayStateListener(fullScreenMediaPlayerController);
        textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        textureView.start();
    }

    private void setSystemUiVisibility(final boolean visible) {
        int newVis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        if (!visible) {
            newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(newVis);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(final int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
                    fullScreenMediaPlayerController.show();
                }
            }
        });
    }
}
