package com.washington.chattertrace.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPostTask {
    private static final Executor executor = Executors.newSingleThreadExecutor();

//    public static void upload(String url, File file) throws IOException {
//        executor.execute(() -> {
//            Log.i("NETWORK", "okhttp");
//            OkHttpClient client = new OkHttpClient();
//            RequestBody formBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("file", file.getName(),
//                            RequestBody.create(MediaType.parse("text/plain"), file))
//                    .addFormDataPart("other_field", "other_field_value")
//                    .build();
//            Request request = new Request.Builder().url(url).post(formBody).build();
//            Response response = null;
//            try {
//                response = client.newCall(request).execute();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            Log.i("NETWORK", response.toString());
//        });
//    }

    public static void upload(String url, File file) {
        executor.execute(() -> {
            try {
                Log.i("NETWORK", "okhttp");
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("files", file.getName(),
                                RequestBody.create(MediaType.parse("text/plain"), file))
                        .build();
                Request request = new Request.Builder().url(url).post(formBody).build();
                Response response = null;
                response = client.newCall(request).execute();
                Log.i("NETWORK", response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void performHttpPost(String url, String requestBody, Context context) {
        executor.execute(() -> {
            Log.i("NETWORK", "EXECUTE");

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                Log.i("NETWORK", "POST");
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);
                try(OutputStream os = urlConnection.getOutputStream()) {
                    Log.i("NETWORK", "WRITE");
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.i("NETWORK", response.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}