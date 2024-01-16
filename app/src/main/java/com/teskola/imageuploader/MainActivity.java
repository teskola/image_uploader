package com.teskola.imageuploader;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button action;
    boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        action = findViewById(R.id.action);
        action.setOnClickListener(this);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel("image_upload");
        running = !(channel == null);
        if (running) {
            action.setText("Stop");
        } else  {
            action.setText("Start");
        }
    }

    @Override
    public void onClick(View v) {
        if (running) {
            stopService(new Intent(this, ImageService.class));
            action.setText("Start");
        }
        else {
            startService(new Intent(this, ImageService.class));
            action.setText("Stop");
        }
        running = !running;

    }
}