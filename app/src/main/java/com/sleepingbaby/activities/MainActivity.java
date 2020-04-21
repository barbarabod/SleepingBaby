package com.sleepingbaby.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sleepingbaby.R;

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
