package com.sleepingbaby;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
