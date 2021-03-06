package com.lightweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lightweather.android.gson.Forecast;
import com.lightweather.android.gson.Weather;
import com.lightweather.android.service.AutoUpdateService;
import com.lightweather.android.util.MyHttpUtil;
import com.lightweather.android.util.MyUtility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherlayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private String m_weatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //initialize all UI object
        weatherlayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text =(TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);

        bingPicImg = (ImageView)findViewById(R.id.bin_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String bingPicurl = prefs.getString("bing_pic",null);
      //  String saved_weatherid = prefs.getString("weather_id",null);
        if(bingPicurl != null)
        {
            Glide.with(this).load(bingPicurl).into(bingPicImg);
        }
        else
        {
            loadBingPic();
        }



        if( weatherString !=null)
        {
            Weather weather = MyUtility.handleWeatherResponse(weatherString);
            m_weatherId =weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else
        {
            m_weatherId = getIntent().getStringExtra("weather_id");
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherlayout.setVisibility(View.INVISIBLE);
            requestWeather(m_weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(m_weatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    public void requestWeather(final String weatherId)
    {

        //String weatherUrl ="https://free-api.heweather.com/v5/now?city="+ weatherId + "&&key=c97caf66a7d94fcf9556699bb56f4a17";
       // http://guolin.tech/api/weather?cityid=CN101010100&key=bc0418b57b2d4918819d3974ac1285d9
        final String weatherUrl ="http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
 //       Toast.makeText(WeatherActivity.this,weatherUrl , Toast.LENGTH_LONG).show();

        //wanggang test httpclient response begin
 /*       new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    OkHttpClient client =new OkHttpClient();
                    Request request = new Request.Builder().url(weatherUrl).build();
                    Response response = client.newCall(request).execute();
                    final String responsedata = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this, responsedata, Toast.LENGTH_LONG).show();
                        }
                    });

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(WeatherActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
*/
//wanggang test httpclient response end


        MyHttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "等会儿再试试吧", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseText = response.body().string();
                String a = response.toString();

                final Weather weather = MyUtility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                        //    editor.putString("weather_id",weatherId);
                            editor.apply();
          //                  Toast.makeText(WeatherActivity.this,responseText , Toast.LENGTH_SHORT).show();

                            showWeatherInfo(weather);
                        }
                        else {
          //                  Toast.makeText(WeatherActivity.this,responseText , Toast.LENGTH_LONG).show();
          //                  Toast.makeText(WeatherActivity.this, "值班的去观察天气了，等会儿再试试吧", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.tmpperature +" ℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
     //   forecastLayout.removeAllViews();

        for(Forecast forecast: weather.forecastList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);


             dateText.setText(forecast.date);


            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
            forecastLayout.setVisibility(View.VISIBLE);
        }
        if(weather.aqi != null)
        {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherlayout.setVisibility(View.VISIBLE);

        //start service
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

    private void loadBingPic()
    {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        MyHttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

}
