package com.flir.flirapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.util.Log;
//import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

//import com.flir.flironeexampleapplication.util.SystemUiHider;
import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;

import java.io.File;
import java.net.Socket;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

/**
 * An example activity and delegate for FLIR One image streaming and device interaction.
 * Based on an example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * <p>
 *
 * @see Device.Delegate
 * @see FrameProcessor.Delegate
 * @see Device.StreamDelegate
 * @see Device.PowerUpdateDelegate
 */
public class GlPreview extends AppCompatActivity implements FrameProcessor.Delegate, Device.StreamDelegate {
    GLSurfaceView thermalSurfaceView;
    private volatile boolean imageCaptureRequested = false;
    private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = true;
    private CameraHandler cameraHandler;
    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;

    private String lastSavedPath;

    private Device.TuningState currentTuningState = Device.TuningState.Unknown;
    // Device Delegate methods

    // Called during device discovery, when a device is connected
    // During this callback, you should save a reference to device
    // You should also set the power update delegate for the device if you have one
    // Go ahead and start frame stream as soon as connected, in this use case
    // Finally we create a frame processor for rendering frames

    public void onDeviceConnected() {
//        Log.i("FlirApp", "Device connected!");

        flirOneDevice = cameraHandler.getFlirOneDevice();
//        flirOneDevice.setPowerUpdateDelegate(this);
        flirOneDevice.startFrameStream(this);

        final ToggleButton chargeCableButton = (ToggleButton) findViewById(R.id.chargeCableToggle);
        if (flirOneDevice instanceof SimulatedDevice) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.VISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.INVISIBLE);
                    findViewById(R.id.connect_sim_button).setEnabled(false);

                }
            });
        }
    }

    /**
     * Indicate to the user that the device has disconnected
     */
//    public void onDeviceDisconnected(Device device) {
//        Log.i("FlirApp", "Device disconnected!");
//
//        final ToggleButton chargeCableButton = (ToggleButton) findViewById(R.id.chargeCableToggle);
//        final TextView levelTextView = (TextView) findViewById(R.id.batteryLevelTextView);
//        final ImageView chargingIndicator = (ImageView) findViewById(R.id.batteryChargeIndicator);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                findViewById(R.id.pleaseConnect).setVisibility(View.GONE);
//                levelTextView.setText("--");
//                chargeCableButton.setChecked(chargeCableIsConnected);
//                chargeCableButton.setVisibility(View.INVISIBLE);
//                chargingIndicator.setVisibility(View.GONE);
//                findViewById(R.id.tuningProgressBar).setVisibility(View.GONE);
//                findViewById(R.id.tuningTextView).setVisibility(View.GONE);
//                findViewById(R.id.connect_sim_button).setEnabled(true);
//            }
//        });
//        flirOneDevice = null;
//    }

    /**
     * If using RenderedImage.ImageType.ThermalRadiometricKelvinImage, you should not rely on
     * the accuracy if tuningState is not Device.TuningState.Tuned
     *
     * @param tuningState
     */
//    public void onTuningStateChanged(Device.TuningState tuningState) {
//        Log.i("FlirApp", "Tuning state changed!");
//
//        currentTuningState = tuningState;
//        if (tuningState == Device.TuningState.InProgress) {
//            runOnUiThread(new Thread() {
//                @Override
//                public void run() {
//                    super.run();
//                    findViewById(R.id.tuningProgressBar).setVisibility(View.VISIBLE);
//                    findViewById(R.id.tuningTextView).setVisibility(View.VISIBLE);
//                }
//            });
//        } else {
//            runOnUiThread(new Thread() {
//                @Override
//                public void run() {
//                    super.run();
//                    findViewById(R.id.tuningProgressBar).setVisibility(View.GONE);
//                    findViewById(R.id.tuningTextView).setVisibility(View.GONE);
//                }
//            });
//        }
//    }


    private ColorFilter originalChargingIndicatorColor = null;

    public void onBatteryChargingStateReceived(final Device.BatteryChargingState batteryChargingState) {
        Log.i("ExampleApp", "Battery charging state received!");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView chargingIndicator = (ImageView) findViewById(R.id.batteryChargeIndicator);
                if (originalChargingIndicatorColor == null) {
                    originalChargingIndicatorColor = chargingIndicator.getColorFilter();
                }
                switch (batteryChargingState) {
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
                        chargingIndicator.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    public void batteryPercentage() {
//        Log.i("ExampleApp", "Battery percentage received!");
        final int percentage=cameraHandler.getBatterypercent();
        final TextView levelTextView = (TextView) findViewById(R.id.batteryLevelTextView);
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                levelTextView.setText(percentage + "%");
            }
        });


    }

    // StreamDelegate method
    public void onFrameReceived(Frame frame) {
        Log.v("ExampleApp", "Frame received!");

        if (currentTuningState != Device.TuningState.InProgress) {
            frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            thermalSurfaceView.requestRender();
        }
    }

    private Bitmap thermalBitmap = null;

    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    public void onFrameProcessed(final RenderedImage renderedImage) {
        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
            // Note: this code is not optimized

            int[] thermalPixels = renderedImage.thermalPixelValues();
            // average the center 9 pixels for the spot meter

            int width = renderedImage.width();
            int height = renderedImage.height();
            int centerPixelIndex = width * (height / 2) + (width / 2);
            int[] centerPixelIndexes = new int[]{
                    centerPixelIndex, centerPixelIndex - 1, centerPixelIndex + 1,
                    centerPixelIndex - width,
                    centerPixelIndex - width - 1,
                    centerPixelIndex - width + 1,
                    centerPixelIndex + width,
                    centerPixelIndex + width - 1,
                    centerPixelIndex + width + 1
            };

            double averageTemp = 0;

            for (int i = 0; i < centerPixelIndexes.length; i++) {
                // Remember: all primitives are signed, we want the unsigned value,
                // we've used renderedImage.thermalPixelValues() to get unsigned values
                int pixelValue = (thermalPixels[centerPixelIndexes[i]]);
                averageTemp += (((double) pixelValue) - averageTemp) / ((double) i + 1);
            }
            double averageC = (averageTemp / 100) - 273.15;
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
            final String spotMeterValue = numberFormat.format(averageC) + "ºC";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.spotMeterValue)).setText(spotMeterValue);
                }
            });
        }

        /*
        Capture this image if requested.
        */
        if (this.imageCaptureRequested) {
            imageCaptureRequested = false;
            final Context context = this;
            new Thread(new Runnable() {
                public void run() {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
                    String formatedDate = sdf.format(new Date());
                    String fileName = "FLIROne-" + formatedDate + ".jpg";
                    try {
                        lastSavedPath = path + "/" + fileName;
                        renderedImage.getFrame().save(new File(lastSavedPath), frameProcessor);

                        MediaScannerConnection.scanFile(context,
                                new String[]{path + "/" + fileName}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> uri=" + uri);
                                    }

                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }



    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    public void onTuneClicked(View v) {
        if (flirOneDevice != null) {
            flirOneDevice.performTuning();
        }

    }

    public void onCaptureImageClicked(View v) {
        if (flirOneDevice != null) {
            this.imageCaptureRequested = true;
        }
    }

    public void onConnectSimClicked(View v) {
        if (flirOneDevice == null) {
            try {
//                flirOneDevice = new SimulatedDevice(this, this, getResources().openRawResource(R.raw.sampleframes), 10);
//                flirOneDevice.setPowerUpdateDelegate(this);
                chargeCableIsConnected = true;
            } catch (Exception ex) {
                flirOneDevice = null;
                Log.w("FLIROneExampleApp", "IO EXCEPTION");
                ex.printStackTrace();
            }
        } else if (flirOneDevice instanceof SimulatedDevice) {
            flirOneDevice.close();
            flirOneDevice = null;
        }
    }

    public void onSimulatedChargeCableToggleClicked(View v) {
        if (flirOneDevice instanceof SimulatedDevice) {
            chargeCableIsConnected = !chargeCableIsConnected;
            ((SimulatedDevice) flirOneDevice).setChargeCableState(chargeCableIsConnected);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gl_preview);
        cameraHandler=CameraDetected.cameraHandler;

        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.BlendedMSXRGBA8888Image;
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage), true);
        frameProcessor.setGLOutputMode(defaultImageType);

        thermalSurfaceView = (GLSurfaceView) findViewById(R.id.imageView95);
        thermalSurfaceView.setPreserveEGLContextOnPause(true);
        thermalSurfaceView.setEGLContextClientVersion(2);
        thermalSurfaceView.setRenderer(frameProcessor);
        thermalSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        thermalSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);



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
        }else {
            onDeviceConnected();
        }
    }

    @Override
    public void onStop() {
        // We must unregister our usb receiver, otherwise we will steal events from other apps
        Log.e("PreviewActivity", "onStop, stopping discovery!");
        Device.stopDiscovery();
        if (flirOneDevice != null) {
            flirOneDevice = null;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (flirOneDevice != null) {
            flirOneDevice.close();
            flirOneDevice = null;
        }
        super.onDestroy();

    }
}
