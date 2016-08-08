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
        startRoutine(); /* call the C fn that begins the ISR thread */
        return START_STICKY;
    }

    /**
     * ====================================================================
     * showToast method:
     *   Used to display Toast messages using Boast and from worker threads
     *   Can be used for debugging to display toasts from
     *   handleStateChange()
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
     *   based on the direction received from the service routine running
     *   through the C pthread, this method will set a string to append to
     *   and intent that will be broadcast by broadcastDirection()
     * ====================================================================
     * authors(s): Stephan Greto-McGrath
     * ====================================================================
     */
    public void handleStateChange(int direction){
        String direc;
        if (direction == 0){
            direc = "COUNTER_CLOCKWISE";
        }else if (direction == 1){
            direc = "CLOCKWISE";
        }else{
            direc = "invalid";
        }
        Log.d(TAG, direc);
        broadcastDirection(direc);
    }

    /**
     * ====================================================================
     * broadcastDirection method:
     *   Pretty self-explanatory, it broadcasts an intent wil the direction
     *   the encoder was turned in so that other apps my pick it up and
     *   act on the input.
     * ====================================================================
     * authors(s): Stephan Greto-McGrath
     * ====================================================================
     */
    public void broadcastDirection(String direc){
        Intent i = new Intent();
        i.setAction("com.google.hal." + direc);
        MyContext.sendBroadcast(i);
    }

    /* load the library for JNI functionality */
    static {
        System.loadLibrary("rotary-encoder-service");
    }
    /* expose the C function to be called through JNI */
    public static native int startRoutine();

}
