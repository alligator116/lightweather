package com.lightweather.android.util;

import android.widget.Toast;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by gang on 2017/3/23.
 */
public class MyHttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback)
    {
        OkHttpClient client =new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
   //     client.newCall(request).enqueue(callback);

        Call call = client.newCall(request);
        call.enqueue(callback);

    }
}
