package com.flir.flirapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


//import org.jetbrains.annotations.Nullable;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.SimulatedDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


class CameraHandler implements Device.Delegate, Device.PowerUpdateDelegate {

    private static final String TAG = "CameraHandler";
    private Device.TuningState currentTuningState = Device.TuningState.Unknown;
    private boolean chargeCableIsConnected = true;

    private volatile Device flirOneDevice;
    private int batterypercent;
    private Device.BatteryChargingState chargingState;

    public Device.TuningState getCurrentTuningState() {
        return currentTuningState;
    }

    public boolean isChargeCableIsConnected() {
        return chargeCableIsConnected;
    }

    public Device getFlirOneDevice() {
        return flirOneDevice;
    }

    public int getBatterypercent() {
        return batterypercent;
    }

    public Device.BatteryChargingState getChargingState() {
        return chargingState;
    }

    public void onTuningStateChanged(Device.TuningState tuningState) {
//        Log.i("FlirApp", "Tuning state changed!");

        currentTuningState = tuningState;
//        if (tuningState == Device.TuningState.InProgress) {
//        }
    }
    @Override
    public void onAutomaticTuningChanged(boolean deviceWillTuneAutomatically) {
    }

    public void onDeviceConnected(Device device) {
//        Log.i("FlirApp", "Device connected!");

        flirOneDevice = device;
        flirOneDevice.setPowerUpdateDelegate(this);
    }

    public void setPowerUpdate(){
        flirOneDevice.setPowerUpdateDelegate(this);
    }

    /**
     * Indicate to the user that the device has disconnected
     */
    public void onDeviceDisconnected(Device device) {
//        Log.i("FlirApp", "Device disconnected!");
        device.close();
        flirOneDevice = null;
    }

    @Override
    public void onBatteryChargingStateReceived(final Device.BatteryChargingState batteryChargingState) {
//        Log.i("FlirApp", "Battery charging state received!");
        chargingState=batteryChargingState;
    }

    @Override
    public void onBatteryPercentageReceived(final byte percentage) {
//        Log.i("FlirApp", "Battery percentage received!");

        batterypercent=(int) percentage;
    }

    public int startDiscovery(Context context) {
        try {
            Device.startDiscovery(context, this);
            return 0;
        } catch (IllegalStateException e) {
            // it's okay if we've already started discovery
            return 1;
        } catch (SecurityException e) {
            // On some platforms, we need the user to select the app to give us permisison to the USB device.
//            showMessage.show("Please insert FLIR One and select " + getString(R.string.app_name));
            // There is likely a cleaner way to recover, but for now, exit the activity and
            // wait for user to follow the instructions;
            return -1;
        }
    }

    public void stopDiscovery(){
        Device.stopDiscovery();
    }

    public void simulator(Context context) throws Exception {
        flirOneDevice = new SimulatedDevice(this, context, context.getResources().openRawResource(R.raw.sampleframes), 10);
        flirOneDevice.setPowerUpdateDelegate(this);
    }
}
