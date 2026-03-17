// 文件路径：com/bytedance/firstDemo/ui/splash/SplashViewModel.java
package com.bytedance.firstDemo.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;

public class SplashViewModel extends ViewModel {
    private final MutableLiveData<Boolean> readyLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getReadyLiveData() {
        return readyLiveData;
    }

    public void prepare() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            readyLiveData.setValue(true);
        }, 1000);
    }
}
