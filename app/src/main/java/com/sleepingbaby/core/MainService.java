package com.sleepingbaby.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sleepingbaby.R;
import com.sleepingbaby.activities.MainActivity;

import static com.sleepingbaby.App.CHANNEL_ID;

public class MainService extends Service
{
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SleepingBaby")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent).build();


        startForeground(364, notification);

        return START_NOT_STICKY;
    }
}
