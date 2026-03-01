package com.example.recipesapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SaladActivity extends AppCompatActivity {

    private MediaPlayer mpSpeach;

    private Button btnStartSpeach;
    private Button btnPauseSpeach;
    private Button btnStopSpeach;

    private TextView tvCurrent;
    private TextView tvTotal;
    private SeekBar seekBarSpeach;
    private ProgressBar progressBarAudio;
    private ConstraintLayout audioControlsContainer;
    private LinearLayout buttonsContainer;

    private Handler handler;
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler = new Handler(Looper.getMainLooper());

        // Get views
        progressBarAudio = findViewById(R.id.progressBarAudio);
        audioControlsContainer = findViewById(R.id.audioControlsContainer);
        buttonsContainer = findViewById(R.id.buttonsContainer);
        btnStartSpeach = findViewById(R.id.btnStart);
        btnPauseSpeach = findViewById(R.id.btnPause);
        btnStopSpeach = findViewById(R.id.btnStop);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvTotal = findViewById(R.id.tvTotal);
        seekBarSpeach = findViewById(R.id.seekBarSpeach);

        // Initialize MediaPlayer asynchronously
        mpSpeach = new MediaPlayer();
        try {
            mpSpeach.setDataSource(getResources().openRawResourceFd(R.raw.salad_speach));
            mpSpeach.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Hide loading, show controls
                    progressBarAudio.setVisibility(View.GONE);
                    audioControlsContainer.setVisibility(View.VISIBLE);
                    buttonsContainer.setVisibility(View.VISIBLE);

                    // Set duration
                    int duration = mp.getDuration();
                    seekBarSpeach.setMax(duration);
                    tvTotal.setText(millisecondsToTime(duration));
                }
            });
            mpSpeach.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            progressBarAudio.setVisibility(View.GONE);
        }

        seekBarSpeach.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mpSpeach != null) {
                    mpSpeach.seekTo(progress);
                    tvCurrent.setText(millisecondsToTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mpSpeach != null && mpSpeach.isPlaying()) {
                    int currentPosition = mpSpeach.getCurrentPosition();
                    seekBarSpeach.setProgress(currentPosition);
                    tvCurrent.setText(millisecondsToTime(currentPosition));
                }
                handler.postDelayed(this, 100);
            }
        };

        btnStartSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpSpeach != null) {
                    mpSpeach.start();
                    handler.post(updateSeekBarRunnable);
                }
            }
        });
        btnPauseSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpSpeach != null) {
                    mpSpeach.pause();
                }
            }
        });
        btnStopSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpSpeach != null) {
                    mpSpeach.stop();
                    mpSpeach.prepareAsync();
                    mpSpeach.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            int duration = mp.getDuration();
                            seekBarSpeach.setMax(duration);
                            tvTotal.setText(millisecondsToTime(duration));
                        }
                    });
                    seekBarSpeach.setProgress(0);
                    tvCurrent.setText("00:00");
                }
            }
        });
    }

    private String millisecondsToTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mpSpeach != null) {
            if (mpSpeach.isPlaying()) {
                mpSpeach.stop();
            }
            mpSpeach.release();
            mpSpeach = null;
        }
        if (handler != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

}
