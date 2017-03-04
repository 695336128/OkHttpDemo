package com.example.zhang.okhttpdemo.application;

import android.app.Application;

import com.example.zhang.okhttpdemo.BuildConfig;
import com.socks.library.KLog;

/**
 * Created by zhang on 2016/12/19.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        KLog.init(BuildConfig.LOG_DEBUG,"fate");
    }
}
