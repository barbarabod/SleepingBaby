package com.sleepingbaby.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sleepingbaby.App;
import com.sleepingbaby.R;
import com.sleepingbaby.activity.SettingsActivity;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences
        .OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_DAY_LIST = "day";
    public static final String KEY_PREF_TIME_LIST = "time";
    public static final String KEY_PREF_LISTEN_SWITCH = "listening";
    public static final String KEY_PREF_RESET_LIST = "reset";

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        SharedPreferences sharedPref =
                android.preference.PreferenceManager
                        .getDefaultSharedPreferences(getContext());

        String marketPref_reset = sharedPref
                .getString(SettingsFragment.KEY_PREF_RESET_LIST, "-1");

        Boolean switchPref_listen = sharedPref.getBoolean
                (SettingsFragment.KEY_PREF_LISTEN_SWITCH, false);

        String marketPref_day = sharedPref
                .getString(SettingsFragment.KEY_PREF_DAY_LIST, "-1");

        String marketPref_time = sharedPref
                .getString(SettingsFragment.KEY_PREF_TIME_LIST, "-1");

        if(key.equals(KEY_PREF_RESET_LIST)) {
            if (marketPref_reset.equals("reset")) {
                sharedPref.edit().putString(SettingsFragment.KEY_PREF_RESET_LIST, "not_reset").apply();
                sharedPref.edit().putBoolean(SettingsFragment.KEY_PREF_LISTEN_SWITCH, false).apply();
                sharedPref.edit().putString(SettingsFragment.KEY_PREF_DAY_LIST, "1").apply();
                sharedPref.edit().putString(SettingsFragment.KEY_PREF_TIME_LIST, "1").apply();

                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        }

        if(key.equals(KEY_PREF_LISTEN_SWITCH)) {
            if(switchPref_listen) {
                requestAudioPermissions();
            }
        }

        if(key.equals(KEY_PREF_DAY_LIST)) {
            SharedPreferences prefs = App.getInstance().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("day", Integer.parseInt(marketPref_day)).apply();
        }

        if(key.equals(KEY_PREF_TIME_LIST)) {
            SharedPreferences prefs = App.getInstance().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("time", Integer.parseInt(marketPref_time)).apply();
        }
    }

    private void requestAudioPermissions()
    {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        SharedPreferences sharedPref =
                android.preference.PreferenceManager
                        .getDefaultSharedPreferences(getContext());

        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    sharedPref.edit().putBoolean(SettingsFragment.KEY_PREF_LISTEN_SWITCH, false).apply();
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    startActivity(intent);
                }
                return;
            }
        }
    }
}

