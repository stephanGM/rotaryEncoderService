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
//import android.os.Process;
import android.widget.Toast;
import android.util.Log;
import java.io.*;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
//import java.lang.Process;

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


        //TODO root access denied
//        Command cmd=new Command(0,"chmod 202 /sys/class/gpio/export");
//        try {
//            RootShell.getShell(true).add(cmd);
//        }catch(Exception e){
//            Log.d(TAG,e.getMessage());
//            // do something
//        }
//
        //TODO no exception, just doesnt work
//        try {
//            Process sh = Runtime.getRuntime().exec("su", null, new File("/system/bin/"));
//            OutputStream os = sh.getOutputStream();
//            os.write(("chmod 777 /sys/class/gpio/export").getBytes("ASCII"));
//            os.flush();
//            os.close();
//            sh.waitFor();
//        }catch(Exception e){
//            Log.d(TAG,e.getMessage());
//        }


        // TODO setWritable returns false
//        String printable = String.valueOf(changeFilePermission("/sys/class/gpio/export"));
//        Log.d(TAG,printable);

        startRoutine(17, 22);
        return START_STICKY;
    }


    public static void handleStateChange(int direction){
        Log.d(TAG,"handled rn");
    }

//    public static boolean changeFilePermission(final String path) {
//        final File file = new File(path);
//        boolean ret = file.setWritable(true, false);
//        return ret;
//    }

    //TODO write a scroll simulation method

    static {
        System.loadLibrary("rotary-encoder-service");
    }
    public static native int startRoutine(int gpio1, int gpio2);


}
