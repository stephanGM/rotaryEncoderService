package com.google.hal.rotaryencoderservice;

/**
 * ====================================================================
 * EncoderService.java:
 *  Service runs on boot or can be started from ServiceLauncher.java
 *  onStartCommand initiates rotary-encoder-service.c
 * ====================================================================
 * authors(s): Stephan Greto-McGrath
 * ====================================================================
 */
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.os.Process;
import android.widget.Toast;
import android.util.Log;
public class EncoderService extends Service{
    private static final String TAG = "EncoderService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Toast.makeText(this, "GPIO Interface Terminated", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startid)
    {
        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        getInterrupt(17, 22);
        return START_STICKY;
    }


    public static void handleStateChange(int direction){
        Log.d(TAG,"handled rn");
    }

    //TODO write a scorll simulation method

    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native int getInterrupt(int gpio1, int gpio2);


}
