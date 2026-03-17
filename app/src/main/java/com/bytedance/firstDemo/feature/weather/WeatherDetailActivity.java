package com.bytedance.firstDemo.feature.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.negative.CitySelectDialog;
import com.bytedance.firstDemo.feature.negative.WeatherViewModel;
import com.bytedance.firstDemo.feature.negative.data.WeatherResponse;

/**
 * 完整天气详情页面（新建）
 * - 天气、白天卡片、夜间卡片、未来 4 日预报
 * - 支持切换城市
 * - 无双滚动条（未来预报不使用 RecyclerView，而是 LinearLayout 动态添加）
 */
public class WeatherDetailActivity extends AppCompatActivity {

    private WeatherViewModel vm;

    // 顶部天气卡片
    private TextView tvCity;
    private TextView tvWeatherMain;
    private TextView tvTempBig;
    private TextView tvTempRange;
    private TextView btnChangeCity;

    // 白天卡片
    private TextView tvDayWeather;
    private TextView tvDayTemp;
    private TextView tvDayWind;

    // 夜间卡片
    private TextView tvNightWeather;
    private TextView tvNightTemp;
    private TextView tvNightWind;

    // 未来预报（替代 RecyclerView）
    private LinearLayout layoutForecastContainer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather_detail);

        initView();
        initViewModel();
        initCityFromIntent();
    }


    private void initView() {

        // 顶部卡片
        tvCity = findViewById(R.id.tvCity);
        tvWeatherMain = findViewById(R.id.tvWeatherMain);
        tvTempBig = findViewById(R.id.tvTempBig);
        tvTempRange = findViewById(R.id.tvTempRange);
        btnChangeCity = findViewById(R.id.btnChangeCity);

        // 白天卡片
        tvDayWeather = findViewById(R.id.tvDayWeather);
        tvDayTemp = findViewById(R.id.tvDayTemp);
        tvDayWind = findViewById(R.id.tvDayWind);

        // 夜间卡片
        tvNightWeather = findViewById(R.id.tvNightWeather);
        tvNightTemp = findViewById(R.id.tvNightTemp);
        tvNightWind = findViewById(R.id.tvNightWind);

        // 未来预报容器（替代 RecyclerView）
        layoutForecastContainer = findViewById(R.id.layoutForecastContainer);

        // 切换城市
        btnChangeCity.setOnClickListener(v -> {
            CitySelectDialog dialog = new CitySelectDialog(this, (cityName, adcode) -> {
                vm.setCityCode(adcode);
                vm.loadWeather();
            });
            dialog.show();
        });
    }


    private void initViewModel() {
        vm = new ViewModelProvider(this).get(WeatherViewModel.class);

        vm.weatherState.observe(this, state -> {
            if (state == null) return;

            switch (state.status) {

                case LOADING:
                    break;

                case SUCCESS:
                    renderWeather(state);
                    break;

                case ERROR:
                    Toast.makeText(this, "天气加载失败：" + state.errorMsg, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        vm.init();
    }


    private void initCityFromIntent() {
        String code = getIntent().getStringExtra("cityCode");
        String name = getIntent().getStringExtra("cityName");

        if (code != null) vm.setCityCode(code);

        vm.loadWeather();
    }


    /**
     * 渲染天气 UI
     */
    private void renderWeather(WeatherViewModel.State s) {

        // 顶部卡片
        tvCity.setText(s.cityName);
        tvWeatherMain.setText(s.weatherText);
        tvTempBig.setText(s.currentTemp);
        tvTempRange.setText("最高：" + s.tempHigh + "°   最低：" + s.tempLow + "°");

        // 白天卡片
        tvDayWeather.setText(s.dayWeather);
        tvDayTemp.setText(s.dayTemp);
        tvDayWind.setText(s.dayWind);

        // 夜间卡片
        tvNightWeather.setText(s.nightWeather);
        tvNightTemp.setText(s.nightTemp);
        tvNightWind.setText(s.nightWind);

        // ===============================
        // 未来预报（使用 LinearLayout 动态添加）
        // ===============================
        layoutForecastContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        if (s.forecastList != null) {
            int count = Math.min(s.forecastList.size(), 4);  // 只显示 4 天
            for (int i = 0; i < count; i++) {

                WeatherResponse.Casts cast = s.forecastList.get(i);

                View item = inflater.inflate(R.layout.item_forecast_full, layoutForecastContainer, false);

                TextView tvWeek = item.findViewById(R.id.tvWeek);
                TextView tvDate = item.findViewById(R.id.tvDate);
                TextView tvWeather = item.findViewById(R.id.tvWeather);
                TextView tvTemp = item.findViewById(R.id.tvTemp);

                // 星期
                tvWeek.setText(toWeekText(cast.week));

                // 日期
                if (cast.date != null && cast.date.length() >= 10) {
                    tvDate.setText(cast.date.substring(5));  // MM-dd
                } else {
                    tvDate.setText("--");
                }

                // 天气
                tvWeather.setText(cast.dayweather);

                // 温度
                tvTemp.setText(cast.daytemp + "° / " + cast.nighttemp + "°");

                layoutForecastContainer.addView(item);
            }
        }
    }


    /**
     * 工具方法：将 1-7 转为 周一 ~ 周日
     */
    private String toWeekText(String w) {
        if (w == null) return "--";
        switch (w) {
            case "1": return "周一";
            case "2": return "周二";
            case "3": return "周三";
            case "4": return "周四";
            case "5": return "周五";
            case "6": return "周六";
            case "7": return "周日";
        }
        return "--";
    }
}
