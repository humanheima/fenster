package com.malmstein.fenster.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.malmstein.fenster.R;
import com.malmstein.fenster.helper.ScreenResolution;
import com.malmstein.fenster.play.FensterPlayer;
import com.malmstein.fenster.play.FensterVideoStateListener;
import com.malmstein.fenster.view.BrightnessProgressBar;
import com.malmstein.fenster.view.VolumeProgressBar;

import java.util.Formatter;
import java.util.Locale;


public final class CopySimpleMediaFensterPlayerController extends FrameLayout implements FensterPlayerController, FensterVideoStateListener {

    public static final String TAG = "CopyPlayerController";
    public static final int DEFAULT_VIDEO_START = 0;
    private static final int DEFAULT_TIMEOUT = 5000;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private ViewConfiguration viewConfiguration;
    private int TOUCH_SLOP;


    private FensterPlayerControllerVisibilityListener visibilityListener;
    private FensterPlayer mFensterPlayer;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mLoading;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private View bottomControlsRoot;
    private View controlsRoot;
    private ProgressBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;

    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private RelativeLayout loadingView;
    private LinearLayout llBrightness;
    private BrightnessProgressBar mBrightness;
    private LinearLayout llVolume;
    private VolumeProgressBar mVolume;
    private int lastPlayedSeconds = -1;
    private boolean rootViewShowing = true;
    private RelativeLayout rlControllerView;

    //是否处于横屏状态下
    private boolean landscape;
    //横屏的时候是否处于屏幕的左半部分，在左半部分上下滑动调节亮度，在右半部分上下滑动调节音量
    private boolean inLeftArea;

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public CopySimpleMediaFensterPlayerController(final Context context) {
        this(context, null);
    }

    public CopySimpleMediaFensterPlayerController(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CopySimpleMediaFensterPlayerController(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initControllerView();
    }

    @Override
    public void setMediaPlayer(final FensterPlayer fensterPlayer) {
        mFensterPlayer = fensterPlayer;
        updatePausePlay();
    }

    @Override
    public void setVisibilityListener(final FensterPlayerControllerVisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    private void initControllerView() {
        viewConfiguration = ViewConfiguration.get(getContext());
        TOUCH_SLOP = viewConfiguration.getScaledTouchSlop();
        LayoutInflater.from(getContext()).inflate(R.layout.copy_fen__view_simple_media_controller, this);
        rlControllerView = (RelativeLayout) findViewById(R.id.rl_controller_view);
        mPauseButton = (ImageButton) findViewById(R.id.fen__media_controller_pause);
        mPauseButton.requestFocus();
        mPauseButton.setOnClickListener(mPauseListener);

        mNextButton = (ImageButton) findViewById(R.id.fen__media_controller_next);
        mNextButton.setOnClickListener(mNextListener);
        mPrevButton = (ImageButton) findViewById(R.id.fen__media_controller_previous);
        mPrevButton.setOnClickListener(mPreviousListener);
        mProgress = (SeekBar) findViewById(R.id.fen__media_controller_progress);
        SeekBar seeker = (SeekBar) mProgress;
        seeker.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);

        mEndTime = (TextView) findViewById(R.id.fen__media_controller_time);
        mCurrentTime = (TextView) findViewById(R.id.fen__media_controller_time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        bottomControlsRoot = findViewById(R.id.fen__media_controller_bottom_area);
        bottomControlsRoot.setVisibility(INVISIBLE);
        controlsRoot = findViewById(R.id.media_controller_controls_root);
        controlsRoot.setVisibility(INVISIBLE);
        loadingView = (RelativeLayout) findViewById(R.id.rl_loading);

        llBrightness = (LinearLayout) findViewById(R.id.ll_brightness);
        mBrightness = (BrightnessProgressBar) findViewById(R.id.seek_bar_brightness);

        llVolume = (LinearLayout) findViewById(R.id.ll_volume);
        mVolume = (VolumeProgressBar) findViewById(R.id.seek_bar_volume);

    }

    /**
     * Show the controller on screen. It will go away
     * automatically after the default time of inactivity.
     */
    @Override
    public void show() {
        Log.e(TAG, "show");
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeInMilliSeconds The timeout in milliseconds. Use 0 to show
     *                           the controller until hide() is called.
     */
    @Override
    public void show(final int timeInMilliSeconds) {
        Log.e(TAG, "show");
        updatePausePlay();
        if (timeInMilliSeconds != -1) {
            if (!mShowing) {
                mShowing = true;
                setProgress();
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
                if (rlControllerView.getVisibility() == INVISIBLE) {
                    rlControllerView.setVisibility(VISIBLE);
                }
                if (llBrightness.getVisibility() == VISIBLE) {
                    llBrightness.setVisibility(GONE);
                }
                if (llVolume.getVisibility() == VISIBLE) {
                    llVolume.setVisibility(GONE);
                }
            }
            // cause the progress bar to be updated even if mShowing
            // was already true.  This happens, for example, if we're
            // paused with the progress bar showing the user hits play.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            Message msg = mHandler.obtainMessage(FADE_OUT);
            if (timeInMilliSeconds != 0) {
                mHandler.removeMessages(FADE_OUT);
                mHandler.sendMessageDelayed(msg, timeInMilliSeconds);
            }
        } else {
            //播放结束的时候显示conroller
            mHandler.removeMessages(SHOW_PROGRESS);
            rlControllerView.setVisibility(VISIBLE);
            if (llBrightness.getVisibility() == VISIBLE) {
                llBrightness.setVisibility(GONE);
            }
            if (llVolume.getVisibility() == VISIBLE) {
                llVolume.setVisibility(GONE);
            }
        }
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        Log.e(TAG, "hide");
        if (mShowing) {
            mShowing = false;
            if (rlControllerView.getVisibility() == VISIBLE) {
                rlControllerView.setVisibility(INVISIBLE);
            }
            updatePausePlay();
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public boolean isLoading() {
        return mLoading;
    }


    private String stringForTime(final int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mFensterPlayer == null || mDragging) {
            return 0;
        }
        int position = mFensterPlayer.getCurrentPosition();
        int duration = mFensterPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mFensterPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }
        final int playedSeconds = position / 1000;
        if (lastPlayedSeconds != playedSeconds) {
            lastPlayedSeconds = playedSeconds;
        }
        return position;
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mFensterPlayer.isPlaying()) {
                mFensterPlayer.start();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mFensterPlayer.isPlaying()) {
                mFensterPlayer.pause();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    public void setVideoPlayPause(boolean play) {
        setVideoPlayPause(play, -1);
    }

    /**
     * @param play true 代表播放 false代表暂停
     */
    public void setVideoPlayPause(boolean play, int seekToPosition) {
        if (mFensterPlayer == null) {
            return;
        }
        if (play) {
            if (seekToPosition != -1) {
                mFensterPlayer.seekTo(seekToPosition);
            }
            mFensterPlayer.start();
        } else {
            mFensterPlayer.pause();
        }
        updatePausePlay();
    }

    private void updatePausePlay() {
        if (mPauseButton == null) {
            return;
        }
        if (mFensterPlayer.isPlaying()) {
            mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mFensterPlayer.isPlaying()) {
            mFensterPlayer.pause();
        } else {
            mFensterPlayer.start();
        }
        updatePausePlay();
    }

    @Override
    public void onFirstVideoFrameRendered() {
        Log.e(TAG, "onFirstVideoFrameRendered");
        bottomControlsRoot.setVisibility(VISIBLE);
        controlsRoot.setVisibility(VISIBLE);
    }

    @Override
    public void onPlay() {
        hideLoadingView();
    }

    @Override
    public void onBuffer() {
        showLoadingView();
    }

    @Override
    public boolean onStopWithExternalError(int position) {
        return false;
    }

    private void hideLoadingView() {
        loadingView.setVisibility(View.GONE);
        mLoading = false;
    }

    private void showLoadingView() {
        mLoading = true;
        loadingView.setVisibility(View.VISIBLE);
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(final SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mFensterPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mFensterPlayer.seekTo((int) newposition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime((int) newposition));
            }
        }

        public void onStopTrackingTouch(final SeekBar bar) {
            mDragging = false;
            updatePausePlay();
            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    if (mFensterPlayer.isPlaying()) {
                        hide();
                    } else {
                        // re-schedule to check again
                        Message fadeMessage = obtainMessage(FADE_OUT);
                        removeMessages(FADE_OUT);
                        sendMessageDelayed(fadeMessage, DEFAULT_TIMEOUT);
                    }
                    break;
                case SHOW_PROGRESS:
                    Log.e(TAG, "show progress");
                    pos = setProgress();
                    if (!mDragging && mFensterPlayer.isPlaying()) {
                        final Message message = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(message, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private final OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(final View v) {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    };

    private final OnClickListener mPreviousListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mFensterPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            if (pos >= 0) {
                mFensterPlayer.seekTo(pos);
                setProgress();
            }
            show(DEFAULT_TIMEOUT);
        }
    };

    private final OnClickListener mNextListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mFensterPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            if (pos <= mFensterPlayer.getDuration()) {
                mFensterPlayer.seekTo(pos);
                setProgress();
            }
            show(DEFAULT_TIMEOUT);
        }
    };

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            show(2000);
            if (landscape) {
                int width = ScreenResolution.getResolution(getContext()).first;
                if (e.getRawX() < width / 2) {
                    inLeftArea = true;
                } else {
                    inLeftArea = false;
                }
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "gestureDetector onScroll: ");
            if (landscape) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > TOUCH_SLOP) {
                        if (deltaX > 0) {
                            Log.e(TAG, "Slide right");
                        } else {
                            Log.e(TAG, "Slide left");
                        }
                    }
                } else {
                    if (Math.abs(deltaY) > TOUCH_SLOP && (Math.abs(deltaY) - Math.abs(deltaX) > 100)) {
                        if (inLeftArea) {
                            //调节亮度
                            llBrightness.setVisibility(VISIBLE);
                            if (deltaY > 0) {
                                Log.e(TAG, "Slide down");
                                mBrightness.setBrightness(false);
                            } else {
                                Log.e(TAG, "Slide up");
                                mBrightness.setBrightness(true);
                            }
                        } else {
                            llVolume.setVisibility(VISIBLE);
                            //调节音量
                            setVolume(-deltaY);
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e(TAG, "gestureDetector onFling: ");
            return true;
        }
    });

    private void setVolume(float delta) {
        int x = (int) delta;
        float progress = mVolume.getProgress();
        if (x < 0) {
            //减小音量
            progress = progress - 1;
        } else {
            progress = progress + 1;
        }
        mVolume.setVolume((int) progress);
    }

}
