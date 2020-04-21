package com.sleepingbaby;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import com.sleepingbaby.core.SleepManager;

public class App extends Application
{
    public static final String CHANNEL_ID = "SleepingBabyChannel";
    private static App instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        createNotificationChannel();

        SleepManager.reset(); // DEBUG
    }

    private void createNotificationChannel()
    {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "SleepingBaby", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    public static App getInstance()
    {
        return instance;
    }
}
