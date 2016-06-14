package com.google.hal.rotaryencoderservice;

/**
 * Created by HAL on 16-06-14.
 */
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;

public class ServiceAutostart extends BroadcastReceiver{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,EncoderService.class);
        arg0.startService(intent);
        Log.i("ServiceAutostart", "started");
    }
}
