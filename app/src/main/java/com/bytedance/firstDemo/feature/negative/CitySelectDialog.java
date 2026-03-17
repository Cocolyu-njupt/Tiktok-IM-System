package com.bytedance.firstDemo.feature.negative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.bytedance.firstDemo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CitySelectDialog extends Dialog {

    public interface OnCitySelectListener {
        void onSelect(String cityName, String adcode);
    }

    private OnCitySelectListener listener;

    private final List<String> provinceList = new ArrayList<>();
    private final List<List<City>> cityList = new ArrayList<>();

    private final List<String> currentCityNames = new ArrayList<>();
    private final List<String> currentCityAdcodes = new ArrayList<>();

    private ArrayAdapter<String> cityAdapter;

    public static class City {
        public String name;
        public String adcode;

        public City(String name, String adcode) {
            this.name = name;
            this.adcode = adcode;
        }
    }

    public CitySelectDialog(@NonNull Context context, OnCitySelectListener listener) {
        super(context, R.style.DialogTheme);
        this.listener = listener;
        loadCityData(context);
    }

    private void loadCityData(Context ctx) {
        try {
            InputStream is = ctx.getAssets().open("city_list.json");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            byte[] buffer = baos.toByteArray();

            String json = new String(buffer, "UTF-8");

            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);

                // 省份名
                String province = item.getString("province");
                provinceList.add(province);

                // 城市列表
                JSONArray cityArr = item.getJSONArray("city");
                List<City> list = new ArrayList<>();

                for (int j = 0; j < cityArr.length(); j++) {
                    JSONObject c = cityArr.getJSONObject(j);
                    String name = c.getString("name");
                    String adcode = c.getString("adcode");
                    list.add(new City(name, adcode));
                }

                cityList.add(list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.dialog_city_select);

        ListView lvProvince = findViewById(R.id.lvProvince);
        ListView lvCity = findViewById(R.id.lvCity);

        // 左侧省份
        ArrayAdapter<String> provAdapter =
                new ArrayAdapter<>(getContext(), R.layout.item_province, R.id.tvProvince, provinceList);
        lvProvince.setAdapter(provAdapter);

        // 右侧城市
        cityAdapter =
                new ArrayAdapter<>(getContext(), R.layout.item_city, R.id.tvCity, currentCityNames);
        lvCity.setAdapter(cityAdapter);

        // 点击省份 → 显示对应城市
        lvProvince.setOnItemClickListener((parent, view, position, id) -> {
            currentCityNames.clear();
            currentCityAdcodes.clear();

            for (City c : cityList.get(position)) {
                currentCityNames.add(c.name);
                currentCityAdcodes.add(c.adcode);
            }

            cityAdapter.notifyDataSetChanged();
        });

        // 点击城市 → 返回城市名 & adcode
        lvCity.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onSelect(currentCityNames.get(position),
                        currentCityAdcodes.get(position));
            }
            dismiss();
        });
    }
}
