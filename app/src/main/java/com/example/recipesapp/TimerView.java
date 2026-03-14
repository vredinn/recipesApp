package com.example.recipesapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class TimerView extends LinearLayout {

  private TextView mLabel;
  private TextView mTimeText;
  private MaterialButton mStartBtn;
  private MaterialButton mPauseBtn;
  private MaterialButton mResetBtn;

  private long mInitialTimeMs = 600000L; // 10 min default
  private long mCurrentTimeMs;
  private boolean mIsRunning;
  private boolean mIsPaused;
  private boolean mIsEditable;
  private int mSoundResId = 0;
  private CountDownTimer mCountDownTimer;
  private Handler mHandler = new Handler(Looper.getMainLooper());
  private MediaPlayer mMediaPlayer;

  public TimerView(Context context) {
    super(context);
    init(context, null, 0);
  }

  public TimerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr) {
    LayoutInflater.from(context).inflate(R.layout.timer_view, this, true);

    mLabel = findViewById(R.id.timer_label);
    mTimeText = findViewById(R.id.timer_time);
    mStartBtn = findViewById(R.id.btn_start);
    mPauseBtn = findViewById(R.id.btn_pause);
    mResetBtn = findViewById(R.id.btn_reset);

    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimerView, defStyleAttr, 0);
      mInitialTimeMs = a.getInt(R.styleable.TimerView_initialTimeMs, 600000);
      mSoundResId = a.getResourceId(R.styleable.TimerView_soundRes, 0);
      mIsEditable = a.getBoolean(R.styleable.TimerView_isEditable, false);
      String label = a.getString(R.styleable.TimerView_label);
      if (label != null) {
        mLabel.setText(label);
      }
      a.recycle();
    }

    mCurrentTimeMs = mInitialTimeMs;
    updateTimeText();
    updateButtons();

    mTimeText.setOnClickListener(v -> showEditDialog());

    mStartBtn.setOnClickListener(v -> startTimer());
    mPauseBtn.setOnClickListener(v -> pauseResumeToggle());
    mResetBtn.setOnClickListener(v -> resetTimer());
  }

  private void startTimer() {
    if (mIsRunning) return;

    mIsPaused = false;
    mCountDownTimer = new CountDownTimer(mCurrentTimeMs, 1000L) {
      @Override
      public void onTick(long millisUntilFinished) {
        mCurrentTimeMs = millisUntilFinished;
        mHandler.post(TimerView.this::updateTimeText);
      }

      @Override
      public void onFinish() {
        mIsRunning = false;
        playSound();
        updateButtons();
      }
    };
    mCountDownTimer.start();
    mIsRunning = true;
    updateButtons();
  }

  private void pauseResumeToggle() {
    if (!mIsRunning) return;
    if (mIsPaused) {
      // resume
      startTimer();
    } else {
      // pause
      if (mCountDownTimer != null) {
        mCountDownTimer.cancel();
      }
      mIsPaused = true;
      updateButtons();
    }
  }

  private void resetTimer() {
    if (mCountDownTimer != null) {
      mCountDownTimer.cancel();
      mCountDownTimer = null;
    }
    mIsRunning = false;
    mIsPaused = false;
    mCurrentTimeMs = mInitialTimeMs;
    mHandler.post(this::updateTimeText);
    updateButtons();
  }

  private void updateTimeText() {
    long minutes = mCurrentTimeMs / 60000;
    long seconds = (mCurrentTimeMs / 1000) % 60;
    String timeStr = String.format("%02d:%02d", minutes, seconds);
    mTimeText.setText(timeStr);

    if (mCurrentTimeMs < 60000) {
      mTimeText.setTextColor(ContextCompat.getColor(getContext(), R.color.timer_red));
    } else {
      mTimeText.setTextColor(Color.BLACK);
    }
  }

  private void updateButtons() {
    mStartBtn.setEnabled(!mIsRunning);
    mPauseBtn.setEnabled(mIsRunning);
    mResetBtn.setEnabled(mIsRunning || mIsPaused);
  }

  private void showEditDialog() {
    if (!mIsEditable || mIsRunning || mIsPaused) return;

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle("@string/timer_edit_title");

    final EditText input = new EditText(getContext());
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setText(formatMs(mInitialTimeMs));
    builder.setView(input);

    builder.setPositiveButton("OK", (dialog, which) -> {
      try {
        String str = input.getText().toString().trim();
        long ms = parseMmss(str);
        if (ms > 0) {
          mInitialTimeMs = ms;
          resetTimer();
        }
      } catch (Exception e) {
        // ignore
      }
    });
    builder.setNegativeButton("Cancel", null);
    builder.show();
  }

  private String formatMs(long ms) {
    long min = ms / 60000;
    long sec = (ms / 1000) % 60;
    return min + ":" + String.format("%02d", sec);
  }

  private long parseMmss(String str) {
    String[] parts = str.split(":");
    if (parts.length != 2) return 0;
    try {
      int min = Integer.parseInt(parts[0]);
      int sec = Integer.parseInt(parts[1]);
      return (min * 60L + sec) * 1000L;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void playSound() {
    try {
      if (mSoundResId == 0) return;
      mMediaPlayer = MediaPlayer.create(getContext(), mSoundResId);
      if (mMediaPlayer != null) {
        mMediaPlayer.setOnCompletionListener(mp -> mp.release());
        mMediaPlayer.start();
      }
    } catch (Exception e) {
      // no sound
    }
  }

  public void cancelTimer() {
    if (mCountDownTimer != null) {
      mCountDownTimer.cancel();
      mCountDownTimer = null;
    }
    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
    mIsRunning = false;
    mIsPaused = false;
    mHandler.post(this::updateTimeText);
    updateButtons();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelTimer();
  }
}

