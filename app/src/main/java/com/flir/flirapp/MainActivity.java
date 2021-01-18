package com.flir.flirapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.flir.thermalsdk.ErrorCode;
//import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
//import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
//import com.flir.thermalsdk.live.CommunicationInterface;
//import com.flir.thermalsdk.live.Identity;
//import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
//import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
//import com.flir.thermalsdk.log.ThermalLog;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Timer timer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                MainActivity.this.finish();
//            }
//        };
//        timer.schedule(timerTask, 1000*3);
//

    }

    public void goReady(View view){
        Intent intent = new Intent(MainActivity.this, DiscoverCamera.class);
        startActivity(intent);
    }

}