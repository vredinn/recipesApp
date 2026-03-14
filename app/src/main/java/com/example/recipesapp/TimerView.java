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
    mHandler.post(this::updateTimeText);
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
        mCurrentTimeMs = mInitialTimeMs;
        updateTimeText();
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
      mIsRunning = false;
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
    if (!isAttachedToWindow()) return;
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
    mPauseBtn.setEnabled(mIsRunning && !mIsPaused);
    mResetBtn.setEnabled(mIsRunning || mIsPaused);
  }

  private void showEditDialog() {
    if (!mIsEditable || mIsRunning) return;

    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.timer_edit_dialog, null);
    EditText etMinutes = dialogView.findViewById(R.id.et_minutes);
    EditText etSeconds = dialogView.findViewById(R.id.et_seconds);

    long min = mInitialTimeMs / 60000;
    long sec = (mInitialTimeMs / 1000) % 60;
    etMinutes.setText(String.valueOf(min));
    etSeconds.setText(String.valueOf(sec));

    new androidx.appcompat.app.AlertDialog.Builder(getContext())
        .setTitle(R.string.timer_edit_title)
        .setView(dialogView)
        .setPositiveButton(R.string.timer_ok, (d, which) -> {
          try {
            int minutes = Integer.parseInt(etMinutes.getText().toString().trim());
            int seconds = Integer.parseInt(etSeconds.getText().toString().trim());
            long ms = ((minutes * 60L + seconds) * 1000L);
            if (ms >= 1000) {
              mInitialTimeMs = ms;
              resetTimer();
            }
          } catch (NumberFormatException e) {
            // ignore
          }
        })
        .setNegativeButton(R.string.timer_cancel, null)
        .show();
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
    try {
      mHandler.removeCallbacksAndMessages(null);
      if (mCountDownTimer != null) {
        mCountDownTimer.cancel();
        mCountDownTimer = null;
      }
      if (mMediaPlayer != null) {
        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
      }
    } catch (Exception e) {
      // ignore
    }
    mIsRunning = false;
    mIsPaused = false;
    updateButtons();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelTimer();
  }
}

