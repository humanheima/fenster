package com.malmstein.fenster.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * Created by dumingwei on 2017/8/21.
 * 音量进度条
 */
public class VolumeProgressBar extends ProgressBar {

    private static final String TAG = "VolumeProgressBar";

    private AudioManager audioManager;
    private static int MAX_VOLUME;


    public VolumeProgressBar(Context context) {
        this(context, null);
    }

    public VolumeProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.e(TAG, "MAX_VOLUME=" + MAX_VOLUME);
        setMax(MAX_VOLUME);
        setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    public void setVolume(int volume) {
        if (volume > MAX_VOLUME) {
            volume = MAX_VOLUME;
        }
        if (volume < 0) {
            volume = 0;
        }
        setProgress(volume);
        if (audioManager == null) {
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerVolumeReceiver();

    }

    @Override
    protected void onDetachedFromWindow() {
        unRegisterVolumeReceiver();
        super.onDetachedFromWindow();
    }

    private void registerVolumeReceiver() {
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        getContext().registerReceiver(volumeReceiver, filter);
    }

    private void unRegisterVolumeReceiver() {
        getContext().unregisterReceiver(volumeReceiver);
    }

    private BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (audioManager == null) {
                audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            }
            setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };
}
