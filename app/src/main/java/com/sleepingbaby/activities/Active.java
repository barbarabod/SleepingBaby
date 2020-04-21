package com.sleepingbaby.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sleepingbaby.R;
import com.sleepingbaby.core.SleepManager;

public class Active extends AppCompatActivity
{
    private static String INFORMATION_CRY = "Click when child start crying";
    private static String INFORMATION_NO_CRY = "Click when child stop crying";
    private static String INFORMATION_WITH_BABY = "Spend time with child";
    private static String BUTTON_CRY = "Child\nCry";
    private static String BUTTON_NO_CRY = "Child\nStopped\nCrying";




    private TextView textInformation;
    private TextView textViewTime;
    private Button buttonCry;


    //TIMER
    private boolean waiting;
    private  boolean withBaby;
    private CountDownTimer countDownTimer;
    private long timeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        SleepManager.reset();


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

    public void onStart(View view)
    {
        if(waiting)
        {
            buttonCry.setText(BUTTON_CRY);
            textInformation.setText(INFORMATION_CRY);
            waiting = false;
            textViewTime.setText("0:00");
            stopTimer();
        }
        else
        {
            buttonCry.setText(BUTTON_NO_CRY);
            textInformation.setText(INFORMATION_NO_CRY);
            waiting = true;
            startTimer();
        }
    }

    public void onReset(View view)
    {

    }

    //TIMER
    public void startTimer()
    {
        int time;
        if(withBaby)
            time = 2100;
        else
            time = SleepManager.getTime();

        countDownTimer = new CountDownTimer(time, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                timeLeft = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish()
            {
                if(withBaby)
                {
                    withBaby = false;
                    buttonCry.setEnabled(true);
                    textInformation.setText(INFORMATION_CRY);
                    buttonCry.setText(BUTTON_CRY);
                }
                else
                {
                    textInformation.setText(INFORMATION_WITH_BABY);
                    withBaby = true;
                    buttonCry.setEnabled(false);
                    SleepManager.updateLastCry();
                    waiting = false;
                    startTimer();
                }

            }
        }.start();
    }



    public void stopTimer()
    {
        countDownTimer.cancel();
    }

    public void updateTimer()
    {
        int minutes = (int) timeLeft / 60000;
        int seconds = (int) timeLeft % 60000 / 1000;

        String timeLeftString = "" + minutes + ":";
        if(seconds < 10) timeLeftString += "0";
        timeLeftString += seconds;

        textViewTime.setText(timeLeftString);
    }

}

