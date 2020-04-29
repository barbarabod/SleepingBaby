package com.sleepingbaby.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.sleepingbaby.App;

import java.time.LocalDate;
import java.time.LocalTime;

public class SleepManager
{
    private static int[][] periods = {{10200, 12200, 15200}, {4200, 5200, 6200}, {4200, 7200, 12000}};
    private static long timeWitChild = 10200;


    public static void reset()
    {
        SharedPreferences prefs = App.getInstance().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("day", -1);
        editor.putInt("repetition", -1);
        editor.putString("lastCryDate", "");
        editor.putString("lastCryTime", "");
        editor.apply();
    }

    public static void updateLastCry()
    {

        SharedPreferences prefs = App.getInstance().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String lastCryDate = prefs.getString("lastCryDate", "");
        String lastCryTime = prefs.getString("lastCryTime", "");

        int day = prefs.getInt("day", -1);
        int repetition = prefs.getInt("repetition", -1);

        if(lastCryDate.equals(""))
        {
            editor.putInt("day", 0);
            editor.putInt("repetition", 0);
        }
        else
        {
            LocalDate lastDate = LocalDate.parse(lastCryDate);
            LocalTime lastTime = LocalTime.parse(lastCryTime);

            if(lastDate.compareTo(LocalDate.now()) == 0)
            {
                if(lastTime.compareTo(LocalTime.NOON) < 0 && LocalTime.now().compareTo(LocalTime.NOON) > 0)
                {
                    editor.putInt("day", day + 1);
                    editor.putInt("repetition", 0);
                }
                else editor.putInt("repetition", repetition + 1);
            }
            else
            {
                if(lastDate.compareTo(LocalDate.now()) == -1)
                {
                    if(lastTime.compareTo(LocalTime.NOON) < 0 && LocalTime.now().compareTo(LocalTime.NOON) > 0)
                    {
                        editor.putInt("day", day + 1);
                        editor.putInt("repetition", 0);
                    }
                }
                else
                {
                    editor.putInt("day", day + 1);
                    editor.putInt("repetition", 0);
                }

            }
        }
        editor.putString("lastCryDate", LocalDate.now().toString());
        editor.putString("lastCryTime", LocalTime.now().toString());
        editor.apply();
    }


    public static int getTime()
    {
        SharedPreferences prefs = App.getInstance().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        String lastCryDate = prefs.getString("lastCryDate", "");
        String lastCryTime = prefs.getString("lastCryTime", "");
        if(lastCryDate.equals("")) return periods[0][0];
        else
        {
            LocalDate lastDate = LocalDate.parse(lastCryDate);
            LocalTime lastTime = LocalTime.parse(lastCryTime);
            int day = prefs.getInt("day", -1);
            int repetition = prefs.getInt("repetition", -1);

            if(lastDate.compareTo(LocalDate.now()) == 0)
            {
                if(lastTime.compareTo(LocalTime.NOON) < 0 && LocalTime.now().compareTo(LocalTime.NOON) > 0)
                {
                    if(day < periods.length) return periods[day + 1][0];
                    else return periods[periods.length -1][0];
                }
                else
                {
                    if(day < periods.length)
                    {
                        if(repetition < periods[day].length - 1) return periods[day][repetition + 1];
                        return periods[day][periods[day].length - 1];
                    }
                }
            }
            if(day < periods.length)
            {
                if(lastDate.compareTo(LocalDate.now()) == -1)
                {
                    if(lastTime.compareTo(LocalTime.NOON) < 0 && LocalTime.now().compareTo(LocalTime.NOON) > 0)
                        return periods[day + 1][0];
                    else return periods[day][repetition + 1];
                }
                return periods[day + 1][0];
            }
            else return periods[periods.length -1][0];
        }
    }

    public static long getTimeWitChild()
    {
        return timeWitChild;
    }
}
