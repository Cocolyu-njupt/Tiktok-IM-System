package com.bytedance.firstDemo.core;

public class ResourceHelper {

    public static int getDrawableResId(String name) {
        if (name == null) return 0;
        return AppContextHolder.getContext()
                .getResources()
                .getIdentifier(name, "drawable",
                        AppContextHolder.getContext().getPackageName());
    }
}
