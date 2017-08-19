package com.malmstein.fenster.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.malmstein.fenster.controller.CopySimpleMediaFensterPlayerController;
import com.malmstein.fenster.helper.ScreenResolution;
import com.malmstein.fenster.view.CopyFensterVideoView;

public class TestActivity extends Activity  {

    private static final String TAG = "TestActivity";

    private CopyFensterVideoView textureView;
    private CopySimpleMediaFensterPlayerController fullScreenMediaPlayerController;
    private RelativeLayout rlVideoView;
    private int portraitWidth;
    private boolean isLandscape;

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

    private void bindViews() {
        rlVideoView = (RelativeLayout) findViewById(R.id.rl_video_view);
        textureView = (CopyFensterVideoView) findViewById(R.id.play_video_texture);
        fullScreenMediaPlayerController = (CopySimpleMediaFensterPlayerController) findViewById(R.id.play_video_controller);
        setPortrait();
    }

    private void initVideo() {
        textureView.setMediaController(fullScreenMediaPlayerController);
        textureView.setOnInfoListener(onInfoToPlayStateListener);
        textureView.setOnPlayStateListener(fullScreenMediaPlayerController);
       /* textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");*/
        AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(R.raw.big_buck_bunny);
        textureView.setVideo(assetFileDescriptor);
        textureView.start();
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

    private MediaPlayer.OnInfoListener onInfoToPlayStateListener = new MediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
            Log.e(TAG, "onInfo what=" + what);
            if (textureView == null || fullScreenMediaPlayerController == null) {
                return false;
            }
            if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                fullScreenMediaPlayerController.onFirstVideoFrameRendered();
                fullScreenMediaPlayerController.onPlay();
            }
            if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                fullScreenMediaPlayerController.onBuffer();
            }
            if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                fullScreenMediaPlayerController.onPlay();
            }
            return false;
        }
    };

}
