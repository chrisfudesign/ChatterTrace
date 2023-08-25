package com.washington.chattertrace.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpPostTask {
    private static final Executor executor = Executors.newSingleThreadExecutor();

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