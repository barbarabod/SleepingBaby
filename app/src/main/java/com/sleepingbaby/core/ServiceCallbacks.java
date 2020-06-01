package com.sleepingbaby.core;

public interface ServiceCallbacks
{
    void updateInfo(String s);
    void updateStartButtonText(String s);
    void updateStartButtonActivity(boolean active);
    void updateTimerTime(long timeLeft);


    void event(String m); // TEST
    void updateInfoUiThread(String s);
    void updateStartButtonTextUiThread(String s);
    void updateTimerTimeUiThread(long timeLeft);
}
