package com.sleepingbaby;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class Active extends AppCompatActivity
{
    private TextView textViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        initViews();
    }

    void initViews()
    {
        textViewTime = findViewById(R.id.text_view_countdown);
    }

    public void onStart(View view)
    {
        updateCountdownText();
    }

    public void onReset(View view)
    {

    }

    private void updateCountdownText() {
        String strFormatTimeLeft = "03:00";
        textViewTime.setText(strFormatTimeLeft);
    }
}

