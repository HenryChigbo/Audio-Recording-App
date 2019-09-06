package com.bluapp.audiorecording;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import com.bluapp.audiorecording.RecordingService.RecordBinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class RecordingActivity extends AppCompatActivity {
    private ImageButton cancelBtn;
    private ImageButton saveBtn;
    private Chronometer recordTime = null;
    private Intent recordIntent;
    private RecordingService recordSrv;
    private boolean recordBound = false;
    public static String STARTFOREGROUND_ACTION = "action.startforeground";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);
        saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        recordTime = (Chronometer) findViewById(R.id.record_time);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onstartRecordingAudio(false);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDialog();
            }
        });
    }

    private ServiceConnection recordConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecordBinder binder = (RecordBinder) service;
            recordSrv = binder.getService();
            recordBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            recordBound = false;
        }
    };


    private void onstartRecordingAudio(boolean start) {
        if (start) {
            File folder = getAbsoluteFile("/AudioRecording", this);
            if (!folder.exists()) {
                folder.mkdir();
            }
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        } else {
            recordTime.stop();
            recordTime.setBase(SystemClock.elapsedRealtime());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if(recordSrv != null && recordBound){
                recordSrv.saveRecording();
            }
            finish();
        }
    }

    private File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }

    private void cancelDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm cancel");
        builder.setMessage("Are you sure?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if(recordSrv != null && recordBound){
                            recordSrv.removeTempRec();
                            finish();
                        }
                    }
                });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (recordIntent == null) {
            onstartRecordingAudio(true);
            recordIntent = new Intent(this, RecordingService.class);
            recordIntent.setAction(STARTFOREGROUND_ACTION);
            bindService(recordIntent, recordConnection, BIND_AUTO_CREATE | BIND_IMPORTANT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(recordIntent);
            } else {
                startService(recordIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(recordSrv != null && recordBound){
            unbindService(recordConnection);
            stopService(recordIntent);
        }
        recordSrv = null;
        super.onDestroy();
    }


}
