package com.google.hal.rotaryencoderservice;

/**
 * Created by HAL on 16-06-14.
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
public class ServiceLauncher extends AppCompatActivity  {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_hello);
        startService(new Intent(this, EncoderService.class));
        finish();
        Toast.makeText(getBaseContext(), getMsgFromJni(), Toast.LENGTH_LONG).show();
    }

    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native String getMsgFromJni();
}
