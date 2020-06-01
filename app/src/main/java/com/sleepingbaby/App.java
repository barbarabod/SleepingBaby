package com.sleepingbaby;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;


public class App extends Application
{
    public static final String CHANNEL_ID = "SleepingBabyChannel";
    public static final String TIMER_CHANNEL_ID = "TimerChannel";
    private static App instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        createNotificationChannel();
    }

    private void createNotificationChannel()
    {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "SleepingBaby", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        NotificationChannel timerChanel = new NotificationChannel(TIMER_CHANNEL_ID, "SleepingBaby", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(timerChanel);
    }

    public static App getInstance()
    {
        return instance;
    }
}
