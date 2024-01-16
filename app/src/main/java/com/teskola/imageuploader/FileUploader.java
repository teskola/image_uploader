package com.teskola.imageuploader;

import android.util.Log;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FileUploader {

    final SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.getDefault());

    void upload(File file)
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
            InputStream inputStream = Files.newInputStream(file.toPath());

            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket("phone")
                    .object(currentYear + "/" + currentMonth + "/" + currentDay + "/" + formattedTime + ".txt")
                    .stream(inputStream, file.length(), -1)
                    .build());
            Log.d("Response", response.etag());
        } catch (MinioException e) {
            Log.e("Error", e.toString());
            Log.e("HTTP trace", e.httpTrace());
        }
    }
}