package com.sleepingbaby.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.sleepingbaby.R;
import com.sleepingbaby.activity.Active;
import com.sleepingbaby.activity.MainActivity;

import static com.sleepingbaby.App.CHANNEL_ID;
import static com.sleepingbaby.App.TIMER_CHANNEL_ID;

public class MainService extends Service
{
    private static String INFORMATION_CRY;
    private static String INFORMATION_NO_CRY;
    private static String INFORMATION_WITH_BABY;
    private static String BUTTON_CRY;
    private static String BUTTON_NO_CRY;

    private ServiceCallbacks serviceCallbacks;

    private final IBinder binder = new LocalBinder();

    public void setCallbacks(ServiceCallbacks callbacks)
    {
        serviceCallbacks = callbacks;
    }

    public class LocalBinder extends Binder
    {
        public MainService getService()
        {
            return MainService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        INFORMATION_CRY = getResources().getString(R.string.information_cry);
        INFORMATION_NO_CRY = getResources().getString(R.string.information_no_cry);
        INFORMATION_WITH_BABY = getResources().getString(R.string.information_with_baby);
        BUTTON_CRY = getResources().getString(R.string.button_cry);
        BUTTON_NO_CRY = getResources().getString(R.string.button_no_cry);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.foreground_info))
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent).build();

        startForeground(364, notification);
        return START_NOT_STICKY;
    }

    // ##### TIMER #####

    private CountDownTimer countDownTimer;

    private boolean withBaby;
    private boolean waiting;
    private long timeLeft;

    /**
     * Main method to start timer
     */
    public void startTimer()
    {
        long time;
        if(withBaby)
            time = SleepManager.getTimeWitChild();
        else
            time = SleepManager.getTime();

        countDownTimer = new CountDownTimer(time, 100)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                timeLeft = millisUntilFinished;
                if(serviceCallbacks != null)
                    serviceCallbacks.updateTimerTime(millisUntilFinished);
            }

            @Override
            public void onFinish()
            {
                if(withBaby)
                {
                    if(serviceCallbacks != null)
                    {
                        serviceCallbacks.updateInfo(INFORMATION_CRY);
                        serviceCallbacks.updateStartButtonActivity(true);
                        serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                    }
                    pushNotification(false);
                    withBaby = false;
                } else
                {
                    if(serviceCallbacks != null)
                    {
                        serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                        serviceCallbacks.updateStartButtonActivity(false);
                    }
                    pushNotification(true);
                    withBaby = true;
                    waiting = false;
                    SleepManager.updateLastCry();
                    startTimer();
                }
            }
        }.start();
    }

    private void stopTimer()
    {
        countDownTimer.cancel();
    }

    /**
     * Activity calls this function when user clicks 'Baby Cry' button
     */
    public void cryClicked()
    {
        if(serviceCallbacks != null)
        {
            if(waiting)
            {
                waiting = false;
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateInfo(INFORMATION_CRY);
                serviceCallbacks.updateTimerTime(0);
                stopTimer();
            } else
            {
                waiting = true;
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateInfo(INFORMATION_NO_CRY);
                startTimer();
            }
        }
    }

    public void initView()
    {
        if(serviceCallbacks != null)
        {
            if(withBaby)
            {
                serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateStartButtonActivity(false);
            } else if(waiting)
            {
                serviceCallbacks.updateInfo(INFORMATION_NO_CRY);
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            } else
            {
                serviceCallbacks.updateInfo(INFORMATION_CRY);
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            }
        }

    }


    public void pushNotification(boolean withChild)
    {
        String text;
        if(withChild)
            text = getResources().getString(R.string.go_child);
        else
            text = getResources().getString(R.string.leave_child);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle(text)
                .setAutoCancel(true);

        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, Active.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(13, builder.build());
    }

    public boolean isWithBaby()
    {
        return withBaby;
    }
}
