package com.teskola.imageuploader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ImageService extends Service {

    final FileUploader uploader = new FileUploader();

    final int INTERVAL = 60 * 60 * 1000; // Take a image every hour

    final Handler handler = new Handler();
    final Runnable runnable = () -> {
        scheduleNext();
        Log.d("ImageService", "tick");
        takePhoto(data -> {
            try {
                Log.d("ImageService", "Upload to database");
                uploader.uploadPhoto(data);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                Log.e("Error", e.toString());
            }
        });


    };

    public interface Callback {
        void onPictureTaken(byte[] data);
    }

    private void scheduleNext() {
        int next = next();
        Log.d("ImageService", "Next image scheduled in: " + next/1000 + " seconds.");
        aquireWakeLock(next);
        handler.postDelayed(runnable, next);
    }

    private int next() {
        return INTERVAL - (int) (System.currentTimeMillis() % (INTERVAL));
    }

    // https://stackoverflow.com/questions/14277981/android-is-it-possible-to-take-a-picture-with-the-camera-from-a-service-with-no

    private void takePhoto(Callback callback) {

        Camera camera = null;
        // Log.d("Camera count", String.valueOf(Camera.getNumberOfCameras()));
        try {
            camera = Camera.open(0); // Use camera 0
            Log.d("ImageService", "Camera found.");
            try {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(params);
                Log.d("Camera", "Autofocus set.");
            } catch (RuntimeException e) {
                Log.w("Autofocus failed", e.toString());
            }

            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            Log.d("Camera", "Taking picture");
            camera.takePicture(null, null, (data, camera1) -> {
                camera1.release();
                Log.d("Camera", "Image captured.");
                callback.onPictureTaken(data);
            });

        } catch (RuntimeException | IOException e) {
            Log.e("Camera", e.toString());
            e.printStackTrace();
            if (camera != null) camera.release();
        }

    }

    public ImageService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ImageService", "Start");
        scheduleNext();
        return START_STICKY;
    }

    /*
    * Use wakelock to keep cpu (and service) running.
     */
    private void aquireWakeLock(long timeout) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire(timeout);
    }



    @Override
    public void onCreate() {

        // Notification channel required for foreground service

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Max importance to avoid service from shutting down
        // TODO: counter to next capture, timestamp of previous
        manager.createNotificationChannel(new NotificationChannel("image_upload", "ImageService Channel", NotificationManager.IMPORTANCE_LOW));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "image_upload")
                .setContentTitle("ImageUploader")
                .setContentText("Uploading images automatically.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);

        startForeground(100, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d("ImageService", "Stop");
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}