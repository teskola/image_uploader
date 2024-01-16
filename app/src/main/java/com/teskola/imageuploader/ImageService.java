package com.teskola.imageuploader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ImageService extends Service {

    final FileUploader uploader = new FileUploader();


    final int INTERVAL = 1 * 60 * 1000;

    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            long now = System.currentTimeMillis();
            writeToFile("latest.txt", String.valueOf(now), context);
            File dir = context.getFilesDir();
            File file = new File(dir, "latest.txt");
            try {
                uploader.upload(file);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            int next = INTERVAL - (int) (now % (INTERVAL));
            handler.postDelayed(this, next);
        }
    };

    private void writeToFile(String name, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }

    public ImageService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Service", "Start");
        handler.postDelayed(runnable, 0);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("image_upload", "ImageService Channel", NotificationManager.IMPORTANCE_LOW));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "image_upload")
                .setContentTitle("ImageService")
                .setSmallIcon(R.mipmap.ic_launcher);
        startForeground(100, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Stop");
        stopForeground(STOP_FOREGROUND_REMOVE);
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}