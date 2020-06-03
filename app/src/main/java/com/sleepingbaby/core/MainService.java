package com.sleepingbaby.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.sleepingbaby.R;
import com.sleepingbaby.fragment.SettingsFragment;
import com.sleepingbaby.activity.Active;
import com.sleepingbaby.activity.MainActivity;

import java.text.DecimalFormat;

import static com.sleepingbaby.App.CHANNEL_ID;
import static com.sleepingbaby.App.TIMER_CHANNEL_ID;

public class MainService extends Service
{
    private static final String TAG = "MainService";

    private static String INFORMATION_START_CRY;
    private static String INFORMATION_STOP_CRY;
    private static String INFORMATION_WITH_BABY;
    private static String INFORMATION_GO_TO_BABY;
    private static String INFORMATION_LEAVE_CHILD;
    private static String BUTTON_CRY;
    private static String BUTTON_NO_CRY;
    private static String BUTTON_STOP_VIBRATING;

    private ServiceCallbacks serviceCallbacks;

    public class LocalBinder extends Binder
    {
        public MainService getService()
        {
            return MainService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        waitingForCry = true;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setStrings();
        pushForeground();

        SharedPreferences sharedPref =
                android.preference.PreferenceManager
                        .getDefaultSharedPreferences(this);

        Boolean switchPref_listen = sharedPref.getBoolean
                (SettingsFragment.KEY_PREF_LISTEN_SWITCH, false);

        if(switchPref_listen) startRecorder();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onDestroy()
    {
        stopRecorder();
        if(vibrator != null) vibrator.cancel();
        if(countDownTimer != null)  countDownTimer.cancel();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(13);
    }


    // ##### TIMER #####

    private CountDownTimer countDownTimer;
    public Vibrator vibrator;

    private boolean withBaby;
    private boolean waitingForCry;
    private boolean vibratingAfterCry;
    private boolean vibratingAfterWitchChild;
    private boolean cryByUser;


    Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            if(msg.arg1 == 1) startTimer();
            else stopTimer();
        }
    };

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
                if(serviceCallbacks != null)
                    serviceCallbacks.updateTimerTime(millisUntilFinished);
            }

            @Override
            public void onFinish()
            {
                Log.i(TAG, "Timer finished");
                if(withBaby)
                {
                    if(serviceCallbacks != null)
                    {
                        serviceCallbacks.updateInfo(INFORMATION_START_CRY);
                        serviceCallbacks.updateStartButtonActivity(true);
                        serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                    }
                    pushNotification(false);
                    withBaby = false;
                } else
                {
                    if(serviceCallbacks != null)
                    {
                        serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                        serviceCallbacks.updateStartButtonActivity(false);
                    }
                    pushNotification(true);
                    withBaby = true;
                    SleepManager.updateLastCry();
                    //startTimer();
                }
                cryByUser = false;
            }
        }.start();
    }

    private void stopTimer()
    {
        if(countDownTimer != null)
            countDownTimer.cancel();
    }

    /**
     * Activity calls this function when user clicks 'Baby Cry' button
     */
    public void cryClicked()
    {
        if(serviceCallbacks != null)
        {
            if(waitingForCry)
            {
                Log.i(TAG, "cryClicked -> waitingForCry");
                waitingForCry = false;
                cryByUser = true;
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateInfo(INFORMATION_STOP_CRY);
                startTimer();
            } else if(vibratingAfterCry)
            {
                Log.i(TAG, "cryClicked -> vibratingAfterCry");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(13);
                vibratingAfterCry = false;
                vibrator.cancel();
                serviceCallbacks.updateStartButtonText(BUTTON_STOP_VIBRATING);
                serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                serviceCallbacks.updateStartButtonActivity(false);
                startTimer();
            } else if(vibratingAfterWitchChild)
            {
                Log.i(TAG, "cryClicked -> vibratingAfterWitchChild");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(13);
                vibratingAfterWitchChild = false;
                waitingForCry = true;
                vibrator.cancel();
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateInfo(INFORMATION_START_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
                cryRecognition = new CryRecognition(60, 10, 400);
            } else
            {
                Log.i(TAG, "cryClicked -> else");
                waitingForCry = true;
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateInfo(INFORMATION_START_CRY);
                serviceCallbacks.updateTimerTime(0);
                cryByUser = false;
                stopTimer();
            }
        }


//        if(serviceCallbacks != null)
//        {
//            if(waiting)
//            {
//                waiting = false;
//                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
//                serviceCallbacks.updateInfo(INFORMATION_START_CRY);
//                serviceCallbacks.updateTimerTime(0);
//                stopTimer();
//            } else
//            {
//                waiting = true;
//                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
//                serviceCallbacks.updateInfo(INFORMATION_STOP_CRY);
//                startTimer();
//            }
//        }
    }

    public void initView()
    {
        if(serviceCallbacks != null)
        {
            if(waitingForCry)
            {
                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
                serviceCallbacks.updateInfo(INFORMATION_START_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            } else if(vibratingAfterCry)
            {
                serviceCallbacks.updateStartButtonText(BUTTON_STOP_VIBRATING);
                serviceCallbacks.updateInfo(INFORMATION_GO_TO_BABY);
                serviceCallbacks.updateStartButtonActivity(true);
            } else if(vibratingAfterWitchChild)
            {
                serviceCallbacks.updateStartButtonText(BUTTON_STOP_VIBRATING);
                serviceCallbacks.updateInfo(INFORMATION_LEAVE_CHILD);
                serviceCallbacks.updateStartButtonActivity(true);
            } else if(withBaby)
            {
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
                serviceCallbacks.updateStartButtonActivity(false);
            } else
            {
                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
                serviceCallbacks.updateInfo(INFORMATION_STOP_CRY);
                serviceCallbacks.updateStartButtonActivity(true);
            }
        }


//        if(serviceCallbacks != null)
//        {
//            if(withBaby)
//            {
//                serviceCallbacks.updateInfo(INFORMATION_WITH_BABY);
//                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
//                serviceCallbacks.updateStartButtonActivity(false);
//            } else if(waitingForCry)
//            {
//                serviceCallbacks.updateInfo(INFORMATION_STOP_CRY);
//                serviceCallbacks.updateStartButtonText(BUTTON_NO_CRY);
//                serviceCallbacks.updateStartButtonActivity(true);
//            } else
//            {
//                serviceCallbacks.updateInfo(INFORMATION_START_CRY);
//                serviceCallbacks.updateStartButtonText(BUTTON_CRY);
//                serviceCallbacks.updateStartButtonActivity(true);
//            }
//        }

    }


    // ##### AUDIO RECORDER #####

    private AudioRecord recorder;
    private boolean cryingDetected;
    private boolean previousTickCrying;
    private CryRecognition cryRecognition = new CryRecognition(60, 10, 400);

    // the audio recording options
    private static final int RECORDING_RATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // the minimum buffer size needed for audio recording
    private static int BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL, FORMAT);

    private boolean currentlySendingAudio = false;

    public void startRecorder()
    {
        Log.i(TAG, "Starting the audio stream");
        currentlySendingAudio = true;
        startStreaming();
    }

    public void stopRecorder()
    {
        if(currentlySendingAudio)
        {
            Log.i(TAG, "Stopping the audio stream");
            currentlySendingAudio = false;
            recorder.release();
        }
    }

    private void startStreaming()
    {
        Log.i(TAG, "Starting the background thread to read the audio data");
        Thread streamThread = new Thread(() ->
        {
            try
            {
                int rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM);
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                short[] buffer = new short[bufferSize];
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                Log.i(TAG, "Creating the AudioRecord");
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                Log.i(TAG, "AudioRecord recording...");
                recorder.startRecording();


                while(currentlySendingAudio)
                {
                    int readSize = recorder.read(buffer, 0, buffer.length);

                    double maxAmplitude = 0;
                    double db = 0;

                    for(int i = 0; i < readSize; i++)
                    {
                        if(Math.abs(buffer[i]) > maxAmplitude)
                            maxAmplitude = Math.abs(buffer[i]);
                    }

                    if(maxAmplitude != 0)
                        db = 20.0 * Math.log10(maxAmplitude / 32767.0) + 90;

                    cryingDetected = cryRecognition.update(maxAmplitude);


                    if(cryingDetected != previousTickCrying && !cryByUser && !vibratingAfterWitchChild && !vibratingAfterCry && !withBaby)
                    {
                        if(cryingDetected)
                        {
                            Log.i(TAG, "Trying to start timer by crying recognition");
                            waitingForCry = false;
                            if(serviceCallbacks != null)
                            {
                                serviceCallbacks.updateStartButtonTextUiThread(BUTTON_NO_CRY);/////////
                                serviceCallbacks.updateInfoUiThread(INFORMATION_STOP_CRY);
                            }
                            Message message = mHandler.obtainMessage(1, 1, 1);
                            message.sendToTarget();
                            //startTimer();
                        } else
                        {
                            Log.i(TAG, "Trying to stop timer by crying recognition");
                            waitingForCry = true;
                            if(serviceCallbacks != null)
                            {
                                serviceCallbacks.updateStartButtonTextUiThread(BUTTON_CRY);////////////
                                serviceCallbacks.updateInfoUiThread(INFORMATION_START_CRY);
                                serviceCallbacks.updateTimerTimeUiThread(0);
                            }
                            Message message = mHandler.obtainMessage(2, 2, 2);
                            message.sendToTarget();
                            //stopTimer();
                        }
                    }


                    previousTickCrying = cryingDetected;
                    //DEBUG
                    DecimalFormat df2 = new DecimalFormat("#.##");
                    Log.d(TAG, "Amplitude: " + maxAmplitude + " ; DB: " + df2.format(db) + "crying: " + cryingDetected + ", waitingForCry: " + waitingForCry + ", withBaby: " + withBaby + ", vibratingAfterCry: " + vibratingAfterCry + ", vibratingAfterWitchChild:" + vibratingAfterWitchChild);
                    if(serviceCallbacks != null)
                        serviceCallbacks.event("Amplitude: " + maxAmplitude + " ; DB: " + df2.format(db) + " \n crying: " + cryingDetected); // TEST
                }
            } catch(Exception e)
            {
                Log.e(TAG, "Exception recording audio. " + e.getMessage());
                e.printStackTrace();
            }
        });

        streamThread.start();
    }


    // UTIL

    public void setCallbacks(ServiceCallbacks callbacks)
    {
        serviceCallbacks = callbacks;
    }

    public boolean isWithBaby()
    {
        return withBaby;
    }

    public void setStrings()
    {
        INFORMATION_START_CRY = getResources().getString(R.string.information_cry);
        INFORMATION_STOP_CRY = getResources().getString(R.string.information_no_cry);
        INFORMATION_WITH_BABY = getResources().getString(R.string.information_with_baby);
        INFORMATION_GO_TO_BABY = getResources().getString(R.string.information_go_baby);
        INFORMATION_LEAVE_CHILD = getResources().getString(R.string.information_leave_baby);
        BUTTON_CRY = getResources().getString(R.string.button_cry);
        BUTTON_NO_CRY = getResources().getString(R.string.button_no_cry);
        BUTTON_STOP_VIBRATING = getResources().getString(R.string.stop_vibrating);
    }

    public void pushForeground()
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.foreground_info))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        startForeground(364, notification);
    }

    public void pushNotification(boolean withChild)
    {
        Log.i(TAG, "Pushing notification");

        String text;
        if(withChild)
        {
            text = getResources().getString(R.string.go_child);
            vibratingAfterCry = true;
        } else
        {
            text = getResources().getString(R.string.leave_child);
            vibratingAfterWitchChild = true;
        }

        initView();

        if(Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 150, 850, 150, 850}, 1));
        else
            vibrator.vibrate(new long[]{0, 500, 100, 500}, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(text)
                .setAutoCancel(true);

        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, Active.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(13, builder.build());
    }

}
