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
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ImageService extends Service {

    final FileUploader uploader = new FileUploader();

    final int INTERVAL = 1 * 60 * 1000; // Take images every minute

    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            takePhoto(data -> {
                try {
                    uploader.uploadPhoto(data);
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    // Stop runnable on error
                    handler.removeCallbacks(runnable);
                }
            });

            int next = INTERVAL - (int) (now % (INTERVAL));
            handler.postDelayed(this, next);
        }
    };

    public interface Callback {
        void onPictureTaken(byte[] data);
    }

    // https://stackoverflow.com/questions/14277981/android-is-it-possible-to-take-a-picture-with-the-camera-from-a-service-with-no

    private void takePhoto(Callback callback) {

        Camera camera = null;
        // Log.d("Camera count", String.valueOf(Camera.getNumberOfCameras()));
        try {
            camera = Camera.open(0); // Use camera 0
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);

            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();


            camera.takePicture(null, null, (data, camera1) -> {
                callback.onPictureTaken(data);
                camera1.release();
            });

        } catch (RuntimeException | IOException e) {
            Log.e("Camera error", e.toString());
            if (camera != null) camera.release();
        }

    }

    public ImageService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("ImageService", "Start");
        handler.postDelayed(runnable, 0);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        // Notification channel required for foreground service

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("image_upload", "ImageService Channel", NotificationManager.IMPORTANCE_LOW));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "image_upload")
                .setContentTitle("ImageUploader")
                .setContentText("Uploading images automatically.")
                .setSmallIcon(R.mipmap.ic_launcher);
        startForeground(100, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d("ImageService", "Stop");
        stopForeground(STOP_FOREGROUND_REMOVE);
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}