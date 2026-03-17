package com.bytedance.firstDemo.feature.negative;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.weather.WeatherDetailActivity;

/**
 * 负一屏天气页面（简化版）
 * 只显示一个天气卡片：
 * - 城市名
 * - 天气
 * - 当前温度
 * - 高低温
 * - 切换城市
 *
 * 点击天气卡片 → 跳转到 WeatherDetailActivity（完整天气页）
 *
 * 不显示未来预报、不显示白天/夜间详情，为未来功能预留空间。
 */
public class NegativeFragment extends Fragment {

    private WeatherViewModel vm;

    // UI
    private View weatherCard;
    private TextView tvCity, tvTemp, tvWeather, tvRange, tvReportTime;
    private TextView btnChangeCity;
    private TextView tvError;
    private ProgressBar pbLoading;


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_negative, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initViewModel();
        initListeners();
    }


    private void initView(View view) {

        weatherCard = view.findViewById(R.id.weatherCard);

        tvCity = view.findViewById(R.id.tvCity);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvWeather = view.findViewById(R.id.tvWeather);
        tvRange = view.findViewById(R.id.tvRange);
        tvReportTime = view.findViewById(R.id.tvReportTime);

        btnChangeCity = view.findViewById(R.id.btnChangeCity);
        tvError = view.findViewById(R.id.tvWeatherError);
        pbLoading = view.findViewById(R.id.pbWeatherLoading);

        // 默认显示切换城市按钮
        btnChangeCity.setVisibility(View.VISIBLE);
    }


    private void initViewModel() {
        vm = new ViewModelProvider(this).get(WeatherViewModel.class);

        // 设置默认城市（广州）
        vm.setCityCode("440100");

        vm.weatherState.observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state.status) {

                case LOADING:
                    pbLoading.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    pbLoading.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
                    renderWeather(state);
                    break;

                case ERROR:
                    pbLoading.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(state.errorMsg == null ?
                            "天气加载失败，点我重试" : state.errorMsg);
                    break;
            }
        });

        vm.init();
    }


    private void initListeners() {

        // 切换城市
        btnChangeCity.setOnClickListener(v -> {
            CitySelectDialog dialog = new CitySelectDialog(requireContext(), (cityName, adcode) -> {
                vm.setCityCode(adcode);
                vm.loadWeather();
            });
            dialog.show();
        });

        // 错误提示 → 点击重试
        tvError.setOnClickListener(v -> vm.loadWeather());

        // 点击天气卡片 → 进入完整天气详情页
        weatherCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WeatherDetailActivity.class);
            intent.putExtra("cityCode", vm.getCityCode());
            intent.putExtra("cityName", vm.getCityName());
            startActivity(intent);
        });
    }


    private void renderWeather(WeatherViewModel.State s) {
        tvCity.setText(s.cityName);
        tvTemp.setText(s.currentTemp);
        tvWeather.setText(s.weatherText);
        tvRange.setText("最高: " + s.tempHigh + "°  最低: " + s.tempLow + "°");
        tvReportTime.setText("更新时间: " + s.reportTime);
    }
}
