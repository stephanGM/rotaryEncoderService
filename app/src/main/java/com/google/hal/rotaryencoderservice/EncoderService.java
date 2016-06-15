package com.google.hal.rotaryencoderservice;

/**
 * Created by HAL on 16-06-14.
 */
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.os.Process;
import android.widget.Toast;
import android.util.Log;


public class EncoderService extends Service implements Runnable{
    private static final String TAG = "MyService";
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

// This will start ServiceLauncher.java activity (they call each other so prob a bad idea)
//        Intent intents = new Intent(getBaseContext(),ServiceLauncher.class);
//        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intents);

        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_LONG).show();
        (new Thread(new EncoderService())).start();
        Log.d(TAG, "onStart");
        return START_STICKY;
    }

    @Override
    public void run() {
        // Moves the current Thread into the foreground
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        String result = Integer.toString(getInterrupt(17, 22)); //starts thread
        Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
    }


    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native int getInterrupt(int gpio1, int gpio2);


}
