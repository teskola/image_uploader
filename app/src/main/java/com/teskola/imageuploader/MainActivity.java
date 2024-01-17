package com.teskola.imageuploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button action;
    boolean running = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        action = findViewById(R.id.action);
        action.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if service is running

        /*
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel("image_upload");

        running = !(channel == null); */

        if (running) {
            action.setText("Stop");
        } else {
            action.setText("Start");
        }

    }

    private void startService() {
        startService(new Intent(this, ImageService.class));
        action.setText("Stop");
        running = true;
    }

    private void stopService() {
        stopService(new Intent(this, ImageService.class));
        action.setText("Start");
        running = false;
    }


    @Override
    public void onClick(View v) {
        if (running) {
            stopService();
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startService();
        }
    }




}