package com.google.hal.rotaryencoderservice;
/**
 * ====================================================================
 * EncoderService.java:
 *  Service runs on boot or can be started from ServiceLauncher.java
 *  onStartCommand initiates rotary-encoder-service.c with the
 *  function startService()
 * ====================================================================
 * authors(s): Stephan Greto-McGrath
 * ====================================================================
 */
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;

public class EncoderService extends Service{

    private static Context MyContext; /* get context to use from JNI */
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
        MyContext = getApplicationContext(); /* get the context to use later from JNI */
        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStart");
        startRoutine(17, 22); /* call the C fn that begins the ISR thread */
        return START_STICKY;
    }

    /**
     * ====================================================================
     * showToast method:
     *   Used to display Toast messages using Boast and from worker threads
     * ====================================================================
     * Details:
     *   uses a handler in order to make Toast/Boast possible from worker
     *   threads
     * ====================================================================
     * authors(s): Stephan Greto-McGrath
     * ====================================================================
     */
    public void showToast(final String msg) {
        Handler hand = new Handler(MyContext.getMainLooper());
        hand.post(new Runnable() {
            @Override
            public void run() {
                Boast boast1 = Boast.makeText(MyContext, msg, Toast.LENGTH_SHORT);
                boast1.show();
            }
        });
    }

    /**
     * ====================================================================
     * handleStateChange method:
     *   will call showToast to display a string indication the direction
     *   of rotation of the rotary encoder. This method is called through
     *   JNI once the direction is determined by the ISR
     * ====================================================================
     * authors(s): Stephan Greto-McGrath
     * ====================================================================
     */
    public void handleStateChange(int direction){
        String direc;
        if (direction == 0){
            direc = "Counter-Clockwise";
        }else if (direction == 1){
            direc = "Clockwise";
        }else{
            direc = "invalid";
        }
        showToast(direc);
        Log.d(TAG, direc);
    }

    //TODO write a scroll simulation method

    /* load the library for JNI functionality */
    static {
        System.loadLibrary("rotary-encoder-service");
    }
    /* expose the C function to be called through JNI */
    public static native int startRoutine(int gpio1, int gpio2);

}
