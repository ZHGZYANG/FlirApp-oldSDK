package com.flir.flirapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class CameraDetected extends AppCompatActivity {
    private static final String TAG = "CameraDetected";

    private ImageView imageview;
    private TextView textview;
    static protected CameraHandler cameraHandler;
    private PermissionHandler permissionHandler;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_detected);
        // ask for permission
//        String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        boolean permissionGranted = (ContextCompat.checkSelfPermission(this, writePermission) == PackageManager.PERMISSION_GRANTED);
//
//        if (!permissionGranted) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, writePermission)) {
//                showMessage.show("App requires write permission to save photos");
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{writePermission}, 0);
//            }
//        }
        cameraHandler = new CameraHandler();
        permissionHandler = new PermissionHandler(showMessage, CameraDetected.this);
        permissionHandler.doForStoragePermission();
        setupViews();

    }


    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startDiscovery();

                Timer intimershow = new Timer();
                TimerTask intimerTaskshow = new TimerTask() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            cameraHandler.simulator(CameraDetected.this);
                                        } catch (Exception e) {
                                           runOnUiThread(()->{
                                               showMessage.show("Setting simulator error. "+e);
                                           });
                                        }
                                        imageview.setVisibility(View.VISIBLE);
                                        textview.setVisibility(View.VISIBLE);
                                    }
                                });

                            }
                        });
                    }
                };
                intimershow.schedule(intimerTaskshow, 1000 * 4);


                Timer intimer = new Timer();
                TimerTask intimerTask = new TimerTask() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (cameraHandler.getBatterypercent() > 25) {
                                    Intent intent1 = new Intent(CameraDetected.this, RecordProcess.class);
                                    startActivity(intent1);
                                } else { // low battery
                                    Intent intent2 = new Intent(CameraDetected.this, ChargeCamera.class);
                                    startActivity(intent2);
                                }
                            }
                        });
                    }
                };
                intimer.schedule(intimerTask, 1000 * 8);
            }
        }).start();
    }


    @SuppressLint("SetTextI18n")
    private void startDiscovery() {
        runOnUiThread(() -> {
            showMessage.show("Start discovery...");

        });
        int result = cameraHandler.startDiscovery(this);
        if (result == -1) {
            // On some platforms, we need the user to select the app to give us permisison to the USB device.
            runOnUiThread(() -> {
                showMessage.show("Please insert FLIR One and select " + getString(R.string.app_name));
            });
            // There is likely a cleaner way to recover, but for now, exit the activity and
            // wait for user to follow the instructions;
//            finish();
        }
    }

    private void stopDiscovery() {
        cameraHandler.stopDiscovery();
    }


    private ShowMessage showMessage = new ShowMessage() {
        @Override
        public void show(String message) {
            final Toast toast = Toast.makeText(CameraDetected.this, message, Toast.LENGTH_LONG);
            toast.show();
        }

    };


    private void setupViews() {
        imageview = findViewById(R.id.imageView3);
        textview = findViewById(R.id.textView11);
        imageview.setVisibility(View.INVISIBLE);
        textview.setVisibility(View.INVISIBLE);
    }


}