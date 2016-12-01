package ischool.noosphere.smartpowersocket;

import android.app.Application;

import ischool.noosphere.smartpowersocket.network.NetworkWorker;

public class PowerSocketApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkWorker.getInstance().startServer();
    }
}
