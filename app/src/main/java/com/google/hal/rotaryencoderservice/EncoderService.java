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

// This will start ServiceLauncher.java activity YET IT ALREADY STARTS (check manifest)
//        Intent intents = new Intent(getBaseContext(),InterfaceLauncher.class);
//        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intents);

        Toast.makeText(this, "GPIO Interface Running", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        return START_STICKY;
    }

}
