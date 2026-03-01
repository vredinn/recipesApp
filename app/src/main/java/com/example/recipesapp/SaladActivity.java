package com.example.recipesapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        btnStartSpeach = findViewById(R.id.btnStart);
        btnPauseSpeach = findViewById(R.id.btnPause);
        btnStopSpeach = findViewById(R.id.btnStop);

        mpSpeach = MediaPlayer.create(this, R.raw.salad_speach);


        btnStartSpeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpSpeach != null) {
                    mpSpeach.start();
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
                }
            }
        });
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
    }

}
