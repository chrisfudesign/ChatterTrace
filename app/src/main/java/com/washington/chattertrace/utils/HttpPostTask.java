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

    public static void upload(String url, File file, Context context) {
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
                Toast.makeText(context, "File uploaded: " + file.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}