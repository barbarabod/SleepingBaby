package com.sleepingbaby.activitie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sleepingbaby.R;
import com.sleepingbaby.core.MainService;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goStart(View view)
    {
        if(!isServiceRunning())
        {
            Intent serviceIntent = new Intent(this, MainService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        Intent intent = new Intent(this, Active.class);
        startActivity(intent);
    }

    public void goSettings(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goExit(View view)
    {

        if(isServiceRunning())
        {
            Intent serviceIntent = new Intent(this, MainService.class);
            stopService(serviceIntent);
        }

        finish();
        System.exit(0);
    }


    //SERVICE
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.sleepingbaby.core.MainService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
