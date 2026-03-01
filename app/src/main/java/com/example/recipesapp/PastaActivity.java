package com.example.recipesapp;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PastaActivity extends AppCompatActivity {

    VideoView videoView;
    private Button btnStartSpeach;
    private Button btnPauseSpeach;
    private Button btnStopSpeach;
    private SeekBar seekBarVideo;

    private Handler handler;
    private Runnable updateSeekBarRunnable;
    private TextView tvCurrent;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pasta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoView = findViewById(R.id.videoView);
        Uri myVideoUri= Uri.parse( "android.resource://" + getPackageName() + "/" + R.raw.pasta);
        videoView.setVideoURI(myVideoUri);

        handler = new Handler(Looper.getMainLooper());
        seekBarVideo = findViewById(R.id.seekBarVideo);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvTotal = findViewById(R.id.tvTotal);

        btnStartSpeach = findViewById(R.id.btnStart);
        btnPauseSpeach = findViewById(R.id.btnPause);
        btnStopSpeach = findViewById(R.id.btnStop);


        if (videoView != null) {
            int duration = videoView.getDuration();
            seekBarVideo.setMax(duration);
            tvTotal.setText(millisecondsToTime(duration));
        }

        seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && videoView != null) {
                    videoView.seekTo(progress);
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
                if (videoView != null && videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBarVideo.setProgress(currentPosition);
                    tvCurrent.setText(millisecondsToTime(currentPosition));
                }
                handler.postDelayed(this, 500);
            }
        };

        btnStartSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoView.start();
                    handler.post(updateSeekBarRunnable);
                }
            }
        });
        btnPauseSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoView.pause();
                }
            }
        });
        btnStopSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoView.pause();
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            int duration = mp.getDuration();
                            seekBarVideo.setMax(duration);
                            tvTotal.setText(millisecondsToTime(duration));
                        }
                    });
                    seekBarVideo.setProgress(0);
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



}
