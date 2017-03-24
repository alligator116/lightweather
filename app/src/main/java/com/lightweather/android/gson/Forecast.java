package com.lightweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gang on 2017/3/24.
 */
public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public ForecastMore more;

    public class Temperature
    {
        public String max;
        public String min;
    }

    public class ForecastMore
    {
        @SerializedName("txt_d")
        public String info;
    }
}
