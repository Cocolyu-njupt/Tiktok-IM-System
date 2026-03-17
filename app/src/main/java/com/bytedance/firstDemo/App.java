package com.bytedance.firstDemo;

import android.app.Application;

import com.bytedance.firstDemo.core.metrics.MetricsCenter;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MetricsCenter.init(this);
    }
}
