package com.flir.flirapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChargeCamera extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_camera);
    }


    public void goReady(View view){
        Intent intent = new Intent(ChargeCamera.this, RecordProcess.class);
        startActivity(intent);
        ChargeCamera.this.finish();
    }
}