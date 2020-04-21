package com.sleepingbaby.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.sleepingbaby.R;
import com.sleepingbaby.activitie.MainActivity;

import static com.sleepingbaby.App.CHANNEL_ID;

public class MainService extends Service
{
    private static String INFORMATION_CRY = "Click when child start crying";
    private static String INFORMATION_NO_CRY = "Click when child stop crying";
    private static String INFORMATION_WITH_BABY = "Spend time with child";
    private static String BUTTON_CRY = "Child\nCry";
    private static String BUTTON_NO_CRY = "Child\nStopped\nCrying";

    private ServiceCallbacks serviceCallbacks;

    private final IBinder binder = new LocalBinder();
    public void setCallbacks(ServiceCallbacks callbacks)
    {
        serviceCallbacks = callbacks;
    }
    public class LocalBinder extends Binder { public MainService getService() { return MainService.this; }}

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
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
                    else
                    {
                        // TODO
                    }
                    withBaby = false;
                }
                else
                {
                    if(serviceCallbacks != null)
                    {
                        serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                        serviceCallbacks.updateStartButtonActivity(false);
                    }
                    else
                    {
                        // TODO
                    }
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
            }
            else
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
            }
            else if(waiting)
            {
                serviceCallbacks.updateInfo(INFORMATION_NO_CRY);
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            }
            else
            {
                serviceCallbacks.updateInfo(INFORMATION_CRY);
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            }
        }

    }




}
