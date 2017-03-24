package com.lightweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gang on 2017/3/24.
 */
public class Now {
    @SerializedName("tmp")
    public String tmpperature;

    @SerializedName("cond")
    public More more;
    public class More
    {
        @SerializedName("txt")
        public String info;
    }
}

