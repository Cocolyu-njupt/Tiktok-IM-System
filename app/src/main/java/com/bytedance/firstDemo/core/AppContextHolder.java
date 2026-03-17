package com.bytedance.firstDemo.core;

import android.app.Application;
import android.content.Context;

public class AppContextHolder {

    private static Context appContext;

    public static void init(Application application) {
        appContext = application.getApplicationContext();
    }

    public static Context getContext() {
        return appContext;
    }
}
