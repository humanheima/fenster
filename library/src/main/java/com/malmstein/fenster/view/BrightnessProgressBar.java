package com.malmstein.fenster.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.malmstein.fenster.helper.BrightnessHelper;

/**
 * Created by dumingwei on 2017/8/21.
 * 亮度进度条
 */
public class BrightnessProgressBar extends ProgressBar {

    private static final String TAG = "BrightnessProgressBar";
    private static int MIN_BRIGHTNESS = 0;
    private static int MAX_BRIGHTNESS = 255;

    public BrightnessProgressBar(Context context) {
        this(context, null);
    }

    public BrightnessProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public BrightnessProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        setMax(MAX_BRIGHTNESS);
    }

    public void setBrightness(boolean up) {
        int brightness = BrightnessHelper.getBrightness(getContext());
        if (up) {
            brightness += 1;
        } else {
            brightness -= 1;
        }
        if (brightness < MIN_BRIGHTNESS) {
            brightness = MIN_BRIGHTNESS;
        } else if (brightness > MAX_BRIGHTNESS) {
            brightness = MAX_BRIGHTNESS;
        }
        BrightnessHelper.setBrightness(getContext(), brightness);
        setProgress(brightness);
    }
}
