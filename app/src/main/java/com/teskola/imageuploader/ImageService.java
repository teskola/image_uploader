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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ImageService extends Service {

    final FileUploader uploader = new FileUploader();


    final int INTERVAL = 1 * 60 * 1000;

    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            takePhoto((data, camera) -> {
                try {
                    uploader.uploadPhoto(data);
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            });

            int next = INTERVAL - (int) (now % (INTERVAL));
            handler.postDelayed(this, next);
        }
    };

    private void takePhoto(Camera.PictureCallback callback) {

        System.out.println("Preparing to take photo");
        Camera camera = null;

        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            // SystemClock.sleep(1000);
            Camera.getCameraInfo(camIdx, cameraInfo);

            try {
                camera = Camera.open(camIdx);
            } catch (RuntimeException e) {
                System.out.println("Camera not available: " + camIdx);
                camera = null;
                //e.printStackTrace();
            }
            try {
                if (null == camera) {
                    System.out.println("Could not get camera instance");
                } else {
                    System.out.println("Got the camera, creating the dummy surface texture");
                    //SurfaceTexture dummySurfaceTextureF = new SurfaceTexture(0);
                    try {
                        //camera.setPreviewTexture(dummySurfaceTextureF);
                        camera.setPreviewTexture(new SurfaceTexture(0));
                        camera.startPreview();
                    } catch (Exception e) {
                        System.out.println("Could not set the surface preview texture");
                        e.printStackTrace();
                    }
                    camera.takePicture(null, null, callback);
                }
            } catch (Exception e) {
                camera.release();
            }


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
                .setContentTitle("ImageUploader")
                .setContentText("Uploading images automatically.")
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