package com.sleepingbaby.activitie;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sleepingbaby.R;
import com.sleepingbaby.core.MainService;
import com.sleepingbaby.core.ServiceCallbacks;
import com.sleepingbaby.core.SleepManager;

public class Active extends AppCompatActivity implements ServiceCallbacks
{
    private TextView textInformation;
    private TextView textViewTime;
    private Button buttonCry;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        initViews();
    }

    void initViews()
    {
        textInformation = findViewById(R.id.text_information);
        textViewTime = findViewById(R.id.text_view_countdown);
        buttonCry = findViewById(R.id.button_start_pause);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onStop() {
        super.onStop();
        if (bound) {
            myService.setCallbacks(null);
            unbindService(serviceConnection);
            bound = false;
        }
    }

    // ##### SERVICE #####

    private MainService myService;
    private boolean bound = false;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MainService.LocalBinder binder = (MainService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(Active.this);
            myService.initView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            bound = false;
        }
    };

    /**
     * Used to update text above timer
     */
    public void updateInfo(String s)
    {
        textInformation.setText(s);
    }

    public void updateStartButtonText(String s)
    {
        buttonCry.setText(s);
    }

    public void updateStartButtonActivity(boolean active)
    {
        buttonCry.setEnabled(active);
    }

    public void updateTimerTime(long timeLeft)
    {
        int minutes = (int) timeLeft / 60000;
        int seconds = (int) timeLeft % 60000 / 1000;

        String timeLeftString = "" + minutes + ":";
        if(seconds < 10) timeLeftString += "0";
        timeLeftString += seconds;

        textViewTime.setText(timeLeftString);
    }

    // ##### XML FUNCTIONS #####

    public void onStartButton(View view)
    {
        myService.cryClicked();

    }

    public void onReset(View view)
    {

    }
}

