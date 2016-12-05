package ischool.noosphere.smartpowersocket;

import android.app.Application;

import ischool.noosphere.smartpowersocket.network.NetworkWorkerPlain;

public class PowerSocketApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkWorkerPlain.getInstance().startServer();
    }
}
