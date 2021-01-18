package com.flir.flirapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorSpace;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.os.SystemClock;
import android.util.Log;
//import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;

public class RecordProcess extends AppCompatActivity implements FrameProcessor.Delegate, Device.StreamDelegate {
    GLSurfaceView thermalSurfaceView;
    private volatile boolean imageCaptureRequested = false;
    private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = false;
    private CameraHandler cameraHandler;
    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;

    private TextView battery;
    private Chronometer itimer;
    private FloatingActionButton fab;

    private boolean videoRecordFinished;
    private boolean isVideoRecord = false;

    private static int count = 0;


    public void onDeviceConnected() {
        Log.i("FlirApp", "Device connected!");

        flirOneDevice = cameraHandler.getFlirOneDevice();
//        flirOneDevice.setPowerUpdateDelegate(this);
        flirOneDevice.startFrameStream(this);
    }


    private ColorFilter originalChargingIndicatorColor = null;

    public void chargingState() {
        Log.i("FlirApp", "Battery charging state received!");
        Device.BatteryChargingState state = cameraHandler.getChargingState();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView chargingIndicator = (ImageView) findViewById(R.id.batteryChargeIndicatorRp);
                if (originalChargingIndicatorColor == null) {
                    originalChargingIndicatorColor = chargingIndicator.getColorFilter();
                }
                if (state != null)
                    switch (state) {
                        case FAULT:
                        case FAULT_HEAT:
                            chargingIndicator.setColorFilter(Color.RED);
                            chargingIndicator.setVisibility(View.VISIBLE);
                            break;
                        case FAULT_BAD_CHARGER:
                            chargingIndicator.setColorFilter(Color.DKGRAY);
                            chargingIndicator.setVisibility(View.VISIBLE);
                        case MANAGED_CHARGING:
                            chargingIndicator.setColorFilter(originalChargingIndicatorColor);
                            chargingIndicator.setVisibility(View.VISIBLE);
                            break;
                        case NO_CHARGING:
                        default:
                            chargingIndicator.setVisibility(View.INVISIBLE);
                            break;
                    }
            }
        });
    }


    Thread batteryper = new Thread(new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            while (true) {
                cameraHandler.setPowerUpdate();
                chargingState();
                int batteryResult = cameraHandler.getBatterypercent();
                battery.setText(batteryResult + "%     ");
                if (batteryResult < 25 && (cameraHandler.getChargingState() == Device.BatteryChargingState.NO_CHARGING)) {
                    battery.setTextColor(Color.RED);
                    runOnUiThread(() -> {
                        showMessage.show("Low battery! Please charge the camera!");
                    });
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    // StreamDelegate method
    public void onFrameReceived(Frame frame) {
        Log.v("FlirApp", "Frame received!");

        if (cameraHandler.getCurrentTuningState() != Device.TuningState.InProgress) {
            frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            thermalSurfaceView.requestRender();
        }
    }

    private Bitmap thermalBitmap = null;

    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    public void onFrameProcessed(final RenderedImage renderedImage) {
//        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
//            // Note: this code is not optimized
//
//            int[] thermalPixels = renderedImage.thermalPixelValues();
//            // average the center 9 pixels for the spot meter
//
//            int width = renderedImage.width();
//            int height = renderedImage.height();
//            int centerPixelIndex = width * (height / 2) + (width / 2);
//            int[] centerPixelIndexes = new int[]{
//                    centerPixelIndex, centerPixelIndex - 1, centerPixelIndex + 1,
//                    centerPixelIndex - width,
//                    centerPixelIndex - width - 1,
//                    centerPixelIndex - width + 1,
//                    centerPixelIndex + width,
//                    centerPixelIndex + width - 1,
//                    centerPixelIndex + width + 1
//            };
//
//            double averageTemp = 0;
//
//            for (int i = 0; i < centerPixelIndexes.length; i++) {
//                // Remember: all primitives are signed, we want the unsigned value,
//                // we've used renderedImage.thermalPixelValues() to get unsigned values
//                int pixelValue = (thermalPixels[centerPixelIndexes[i]]);
//                averageTemp += (((double) pixelValue) - averageTemp) / ((double) i + 1);
//            }
//            double averageC = (averageTemp / 100) - 273.15;
//            NumberFormat numberFormat = NumberFormat.getInstance();
//            numberFormat.setMaximumFractionDigits(2);
//            numberFormat.setMinimumFractionDigits(2);
//            final String spotMeterValue = numberFormat.format(averageC) + "ºC";
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    ((TextView) findViewById(R.id.spotMeterValue)).setText(spotMeterValue);
//                }
//            });
//        }


        if (this.imageCaptureRequested) {  //image capture
            this.imageCaptureRequested = false;

            new Thread(new Runnable() {
                public void run() {
                    String state = Environment.getExternalStorageState();
                    if (state.equals(Environment.MEDIA_MOUNTED)) {

                        String imagePath = getImageName("photo");

                        try {
//                            renderedImage.getFrame().save(new File(imagePath), frameProcessor);

//                            {
//                                MatFileWriter mfw = new MatFileWriter();
//                                ByteBuffer.wrap(renderedImage.pixelData()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPixels);
//                                int[] matThermalPixels = renderedImage.thermalPixelValues();
//                                int width = renderedImage.width();
//                                int height = renderedImage.height();
//                                int total = height * width;
//                                double[] dataCopy = new double[matThermalPixels.length];
//                                for (int i = 0; i < total; i++) {
//                                    dataCopy[i] = (double) matThermalPixels[i];
//                                }
//                                MLDouble data = new MLDouble(dataName, dataCopy, 240);
//                                ArrayList<MLArray> allData = new ArrayList<MLArray>();
//                                allData.add(data);
//                                mfw.write(new File(lastSavedPath), allData);
//
//                            }

                            {
                                int[] data = renderedImage.thermalPixelValues();
                                ImageInfo imi = new ImageInfo(renderedImage.width(), renderedImage.height(), 16, false, true, false);
                                File img = new File(imagePath);
                                PngWriter png = new PngWriter(img, imi);
                                int width = renderedImage.width();
                                for (int i = 0; i < renderedImage.height(); i++) {
                                    int[] temperature = Arrays.copyOfRange(data, i * width, (i * width) + width);
                                    png.writeRowInt(temperature);
                                }
                                png.end();
                            }
                            runOnUiThread(() -> {
                                showMessage.show("Photo saved.");
                            });

                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                showMessage.show("Save photo failed! " + e);
                            });
                        }

                    } else {
                        runOnUiThread(() -> {
                            showMessage.show("Save photo failed! Media is not mounted.");
                        });
                    }
                }
            }).start();
        }

        if (!videoRecordFinished && isVideoRecord) { //video record
            count++;
            new Thread(new Runnable() {
                public void run() {

                    String state = Environment.getExternalStorageState();
                    if (state.equals(Environment.MEDIA_MOUNTED)) {
                        String videoPath = getImageName("video");

                        {
                            int[] data = renderedImage.thermalPixelValues();
                            ImageInfo imi = new ImageInfo(renderedImage.width(), renderedImage.height(), 16, false, true, false);
                            File img = new File(videoPath);
                            PngWriter png = new PngWriter(img, imi);
                            int width = renderedImage.width();
                            for (int i = 0; i < renderedImage.height(); i++) {
                                int[] temperature = Arrays.copyOfRange(data, i * width, (i * width) + width);
                                png.writeRowInt(temperature);
                            }
                            png.end();
                        }
                    } else {
                        Log.d("error", "Save video failed! Media is not mounted.");
                        runOnUiThread(() -> {
                            showMessage.show("Save video failed! Media is not mounted.");
                        });

                    }
                }
            }).start();

        }

    }

    protected String getImageName(String model) {
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmssSS", Locale.getDefault());
        String fileName = simpleDate.format(now.getTime());

        String dirPath;
        if (model.equals("photo")) {
            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flirapp/image/";
        } else {
            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flirapp/image/temp/";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath + fileName + ".png";
    }

    public void capture(View view) {
        this.imageCaptureRequested = true;
    }

    @SuppressLint("SetTextI18n")
    public void video2(View view) {
        if (!isVideoRecord) {
            isVideoRecord = true;
            videoRecordFinished = false;
            timerStart();
        } else {
            timerStop();
            videoRecordFinished = true;
            isVideoRecord = false;
            runOnUiThread(() -> {
                showMessage.show("Video saving...");
            });

//            videoHandler2();
            // following: folder model
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        runOnUiThread(() -> {
                            showMessage.show("Video Save failed. Error code: 99050");
                        });
                    }
                    Calendar now = new GregorianCalendar();
                    SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                    String fileName = simpleDate.format(now.getTime());
                    String oldDirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flirapp/image/temp/";
                    String newDirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flirapp/image/" + fileName + "/";
                    File oldDir = new File(oldDirName);
                    File newDir = new File(newDirName);
                    boolean success = oldDir.renameTo(newDir);
                    if (success) {
                        runOnUiThread(() -> {
                            showMessage.show("Video Saved. " + count);
                            count = 0;
                        });
                    } else {
                        runOnUiThread(() -> {
                            showMessage.show("Save video faild! Error code: 90074"); // 90074: cannot rename folder
                        });
                    }
                }
            }).start();
        }
    }


    public void onTuneClicked(View v) {
        if (flirOneDevice != null) {
            flirOneDevice.performTuning();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_process);
        cameraHandler = CameraDetected.cameraHandler;

        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.BlendedMSXRGBA8888Image;
//        RenderedImage.ImageType defaultImageType =RenderedImage.ImageType.ThermalLinearFlux14BitImage;
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage), true);
        frameProcessor.setGLOutputMode(defaultImageType);

        thermalSurfaceView = (GLSurfaceView) findViewById(R.id.msx_image);
        thermalSurfaceView.setPreserveEGLContextOnPause(true);
        thermalSurfaceView.setEGLContextClientVersion(2);
        thermalSurfaceView.setRenderer(frameProcessor);
        thermalSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        thermalSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        setupViews();
        batteryper.start();
//        PermissionHandler permissionHandler = new PermissionHandler(showMessage, RecordProcess.this);
//        permissionHandler.doForStoragePermission();
        verifyStoragePermissions(this);
        // ask for permission
//        String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        boolean permissionGranted = false;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            permissionGranted = (ContextCompat.checkSelfPermission(this, writePermission) == PackageManager.PERMISSION_GRANTED);
//        } else {
//            permissionGranted = (PermissionChecker.checkSelfPermission(this, writePermission) == PermissionChecker.PERMISSION_GRANTED);
//        }
//
//        if (!permissionGranted) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, writePermission)) {
//                Toast.makeText(this, "App requires write permission to save photos", Toast.LENGTH_LONG).show();
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{writePermission}, 0);
//            }
//        }
    }

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        thermalSurfaceView.onPause();
        if (flirOneDevice != null) {
            flirOneDevice.stopFrameStream();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        thermalSurfaceView.onResume();
        if (flirOneDevice != null) {
            flirOneDevice.startFrameStream(this);
        } else {
            onDeviceConnected();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                video2(view);
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                capture(view);
                return true;
            }
        });

    }

//    @Override
//    public void onStop() {
//        // We must unregister our usb receiver, otherwise we will steal events from other apps
//        Log.e("PreviewActivity", "onStop, stopping discovery!");
//        Device.stopDiscovery();
//        if (flirOneDevice != null) {
//            flirOneDevice = null;
//        }
//        super.onStop();
//    }

    @Override
    public void onDestroy() {
        Device.stopDiscovery();
        if (flirOneDevice != null) {
            flirOneDevice.close();
            flirOneDevice = null;
        }
        super.onDestroy();

    }

    private ShowMessage showMessage = new ShowMessage() {
        @Override
        public void show(String message) {
            final Toast toast = Toast.makeText(RecordProcess.this, message, Toast.LENGTH_LONG);
            toast.show();
        }

    };

    private void setupViews() {
        itimer = findViewById(R.id.itimer);
        fab = findViewById(R.id.floatingActionButton4);
        battery = findViewById(R.id.battery);
    }

    public void timerStop() {
        itimer.stop();
    }

    public void timerStart() {
        itimer.setBase(SystemClock.elapsedRealtime());
        itimer.start();
    }

}
