package com.bluapp.audiorecording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.bluapp.audiorecording.RecordingActivity.STARTFOREGROUND_ACTION;

public class RecordingService extends Service {
    private final String LOG_TAG = "AndroidRecordingLog";
    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Timer mTimer = null;
    private File file;
    private final IBinder recordBind = new RecordBinder();
    public String CHANNEL_ID = "channel-01";
    public String CHANNEL_NAME = "Recorder";
    private RecordDatabase recordDatabase;
    private String currentDateandTime;
    private String recordlength;


    private static TimerTask mIncrementTimerTask = null;

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        recordDatabase = RecordDatabase.getInstance(this);
    }

    public class RecordBinder extends Binder {
        RecordingService getService() {
            return RecordingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return recordBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.getAction().equals(STARTFOREGROUND_ACTION)) {
                startRecording();
            }
        }
        return START_STICKY;
    }

    public void startRecording() {
        setFileNameAndPath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        //if (MySharedPreferences.getPrefHighQuality(this)) {
       //     mRecorder.setAudioSamplingRate(44100);
        //    mRecorder.setAudioEncodingBitRate(192000);
       // }
        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
            startTimer();
            startForeground(1, createNotification());
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void saveRecording(){
        if (mRecorder != null) {
            stopRecording();
            String filesize;
            long filelength = file.length() / 1024;
            double fileSizeInKB = file.length() / 1024;
            double fileSizeInMB = fileSizeInKB * 1024;
            if (filelength > 1024) {
                filesize = String.valueOf(fileSizeInMB)+" MB";
            }else{
                filesize = String.valueOf(fileSizeInKB)+" KB";
            }
            Record recordcontent = new Record(mFileName, mFilePath, recordlength, false, filesize, currentDateandTime);
            recordDatabase.recordDAO().insert(recordcontent);
        }
    }

    public void setFileNameAndPath(){
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.getDefault());
        currentDateandTime = sdf.format(new Date());
        do{
            count++;
            mFileName = currentDateandTime + ".mp3";
            file = getAbsoluteFile("/AudioRecording/" + mFileName, this);
            mFilePath = file.toString();
        }while (file.exists() && !file.isDirectory());
    }

    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
        mRecorder = null;
    }

    public void removeTempRec(){
        if (file.exists()) {
            file.delete();
        }
}

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    private File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }

    private Notification createNotification() {
        recordlength = mTimerFormat.format(mElapsedSeconds * 1000);
        createNotificationChannel();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_microphone)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(recordlength)
                        .setOngoing(true);
        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0, new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));
        return mBuilder.build();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setLockscreenVisibility(1);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null){
            mTimer.cancel();
        }
        stopForeground(true);
    }

}
