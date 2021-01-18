package com.flir.flirapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class DiscoverCamera extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_camera);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(DiscoverCamera.this, CameraDetected.class);
                startActivity(intent);
                DiscoverCamera.this.finish();
            }
        };
        timer.schedule(timerTask, 1000*3);
    }


//    public void goReady(View view){
//        Intent intent = new Intent(this, CameraDetected.class);
//        startActivity(intent);
//    }
}