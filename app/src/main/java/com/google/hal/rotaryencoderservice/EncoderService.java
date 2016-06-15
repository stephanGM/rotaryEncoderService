package com.google.hal.rotaryencoderservice;

/**
 * Created by HAL on 16-06-14.
 */
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
public class EncoderService extends Service{
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

// This will start ServiceLauncher.java activity
//        Intent intents = new Intent(getBaseContext(),ServiceLauncher.class);
//        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intents);

        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_LONG).show();
        String result = Integer.toString(getInterrupt(17, 22));
        Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        return START_STICKY;
    }

    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native int getInterrupt(int gpio1, int gpio2);


}
