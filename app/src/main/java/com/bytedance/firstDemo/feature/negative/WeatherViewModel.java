package com.bytedance.firstDemo.feature.negative;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.firstDemo.feature.negative.data.WeatherRepository;
import com.bytedance.firstDemo.feature.negative.data.WeatherResponse;
import com.bytedance.firstDemo.feature.negative.data.WeatherUiState;

/**
 * WeatherViewModel（覆盖版）
 * - 保留原有结构
 * - 增加白天/夜间字段处理
 * - 支持城市切换
 * - 不影响其他模块
 */
public class WeatherViewModel extends ViewModel {

    public enum Status { LOADING, SUCCESS, ERROR }

    public static class State {
        public Status status;

        // ======= 原有字段 =======
        public String cityName;
        public String currentTemp;
        public String weatherText;
        public String tempHigh;
        public String tempLow;
        public String reportTime;
        public java.util.List<WeatherResponse.Casts> forecastList;
        public String errorMsg;

        // ======= 新增：白天 / 夜间详情 =======
        public String dayWeather;
        public String dayTemp;
        public String dayWind;

        public String nightWeather;
        public String nightTemp;
        public String nightWind;


        public static State loading() {
            State s = new State();
            s.status = Status.LOADING;
            return s;
        }

        public static State error(String msg) {
            State s = new State();
            s.status = Status.ERROR;
            s.errorMsg = msg;
            return s;
        }

        public static State success(WeatherUiState ui) {
            State s = new State();
            s.status = Status.SUCCESS;

            // ===== 原有字段 =====
            s.cityName = ui.cityName;
            s.currentTemp = ui.currentTemp;
            s.weatherText = ui.weatherText;
            s.tempHigh = ui.tempHigh;
            s.tempLow = ui.tempLow;
            s.reportTime = ui.reportTime;
            s.forecastList = ui.forecastList;

            // ===== 新增字段 =====
            s.dayWeather = ui.dayWeather;
            s.dayTemp = ui.dayTemp;
            s.dayWind = ui.dayWind;

            s.nightWeather = ui.nightWeather;
            s.nightTemp = ui.nightTemp;
            s.nightWind = ui.nightWind;

            return s;
        }
    }


    // ===== ViewModel 核心逻辑 =====
    private WeatherRepository repo = null;
    private final MutableLiveData<State> _weatherState = new MutableLiveData<>();
    public LiveData<State> weatherState = _weatherState;

    private String cityCode = "440100"; // 默认广州


    /**
     * 初始化：只在首次调用时初始化 Repository 并加载天气
     */
    public void init() {
        if (repo == null) {
            repo = new WeatherRepository();
            loadWeather();
        }
    }

    public void setCityCode(String code) {
        cityCode = code;
    }

    public String getCityCode() {
        return cityCode;
    }


    /**
     * 加载天气：负一屏 & 详情页都会调用
     */
    public void loadWeather() {
        _weatherState.postValue(State.loading());

        repo.fetchWeather(cityCode, new WeatherRepository.RepoCallback() {
            @Override
            public void onSuccess(WeatherResponse resp) {
                WeatherUiState ui = WeatherUiState.from(resp);
                _weatherState.postValue(State.success(ui));
            }

            @Override
            public void onError(String msg) {
                _weatherState.postValue(State.error(msg));
            }
        });
    }


    /**
     * 根据城市中文名切换
     */
    public void changeCityByName(String cityName) {
        _weatherState.postValue(State.loading());

        repo.fetchAdcodeByCityName(cityName, new WeatherRepository.AdcodeCallback() {
            @Override
            public void onSuccess(String adcode) {
                cityCode = adcode;
                loadWeather(); // 刷新天气
            }

            @Override
            public void onError(String msg) {
                _weatherState.postValue(State.error(msg));
            }
        });
    }


    /**
     * 城市名（给 NegativeFragment & WeatherDetail 使用）
     */
    public String getCityName() {
        if (weatherState.getValue() == null) return "";
        return weatherState.getValue().cityName;
    }
}
