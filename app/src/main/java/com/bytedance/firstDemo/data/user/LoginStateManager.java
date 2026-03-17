package com.bytedance.firstDemo.data.user;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginStateManager {
    private static final String PREF_NAME = "login_state";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public static String getCurrentNickname(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("nickname", "用户昵称");
    }

    public static void setAccount(Context context, String account) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("account", account).apply();
    }

    public static String getCurrentAccount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("account", "");
    }

}

