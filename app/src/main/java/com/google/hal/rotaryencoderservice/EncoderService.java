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

// TODO clean up imports and libs once permissions are fixed
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;
import android.util.Log;

public class EncoderService extends Service{

    public EncoderService(){

    }
    private static final String TAG = "EncoderService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Toast.makeText(this, "GPIO Interface Terminated", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startid)
    {
        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStart");

        startRoutine(17, 22);
        return START_STICKY;
    }

    public void showToast(final String sText){
        final Context MyContext = this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run(){
                Boast boast1 = Boast.makeText(MyContext, sText, Toast.LENGTH_SHORT);
                boast1.show();
            }
        });
    };

    public void handleStateChange(int direction){
        String direc;
        if (direction == 0){
            direc = "Counter-Clockwise";
        }else if (direction == 1){
            direc = "Clockwise";
        }else{
            direc = "invalid";
        }
//        final Context MyContext = this;
//        Toast boast1 = Toast.makeText(MyContext, direc, Toast.LENGTH_SHORT);
//        boast1.show();
        Log.d(TAG, direc);
    }



    //TODO write a scroll simulation method

    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native int startRoutine(int gpio1, int gpio2);


}
