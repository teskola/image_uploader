package com.teskola.imageuploader;

import android.util.Log;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FileUploader {

    final SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.getDefault());

    /*
        Upload picture to Min.io database
     */
    void uploadPhoto(byte[] data)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(BuildConfig.ENDPOINT)
                            .credentials(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY)
                            .build();

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            String formattedTime = sdf.format(new Date());
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            Log.d("Data length", String.valueOf(data.length));
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket("phone")
                    .object(currentYear + "/" + currentMonth + "/" + currentDay + "/" + formattedTime + ".jpg")
                    .contentType("image/jpg")
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .build());
            Log.d("File uploaded", response.object());
        } catch (MinioException e) {
            Log.e("Error", e.toString());
            Log.e("HTTP trace", e.httpTrace());
        }
    }
}