package com.lightweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lightweather.android.db.City;
import com.lightweather.android.db.Country;
import com.lightweather.android.db.Province;
import com.lightweather.android.util.MyHttpUtil;
import com.lightweather.android.util.MyUtility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by gang on 2017/3/23.
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel == LEVEL_PROVINCE)
                {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY)
                {
                    selectedCity=cityList.get(i);
                    queryCountries();
                }
                else if(currentLevel == LEVEL_COUNTRY)
                {
                    String weatherId = countryList.get(i).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                 //   getActivity().finish();
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTRY)
                {
                    queryCities();
                    return;
                }
                if(currentLevel == LEVEL_CITY)
                {
                    queryProvinces();
                    return;
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces()
    {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0)
        {
            dataList.clear();
            for(Province province: provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else
        {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");

        }
    }

    private void queryCities()
    {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId= ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0)
        {
            dataList.clear();
            for(City city: cityList)
            {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCountries()
    {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size()>0)
        {
            dataList.clear();
            for(Country country: countryList)
            {
                dataList.add(country.getCountryName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "country");
        }
    }

    private void queryFromServer(String address, final String leveltype)
    {
        showProgressDialog();
        MyHttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(leveltype))
                {
                    result = MyUtility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(leveltype))
                {
                    result = MyUtility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("country".equals(leveltype))
                {
                    result = MyUtility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(leveltype))
                            {
                                queryProvinces();
                            }
                            else if("city".equals(leveltype))
                            {
                                queryCities();
                            }
                            else if("country".equals(leveltype))
                            {
                                queryCountries();
                            }
                        }
                    });
                }

            }
        });
    }

    private void showProgressDialog()
    {
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog()
    {
        if(progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }
}
