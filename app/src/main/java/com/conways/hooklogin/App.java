package com.conways.hooklogin;

import android.app.Application;

public class App extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        HookManager.getInstance(this).startHook();
    }
}
