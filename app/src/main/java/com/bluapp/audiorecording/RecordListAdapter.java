package com.bluapp.audiorecording;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class RecordListAdapter extends AbstractItem<RecordListAdapter, RecordListAdapter.ViewHolder> implements MediaStopObs.MediaStopInterface {


    private final int VIEW_TYPE = 2222;
    private RecordListModel recordListModel;
    private Context context;
    private static MediaPlayer player;
    private int playingposition;
    private int viewposition = -1;


    public RecordListAdapter(RecordListModel recordListModel, Context context) {
        this.context = context;
        this.recordListModel = recordListModel;
        this.playingposition = -1;
        MediaStopObs.getInstance().setListener(this);
    }

    public RecordListModel getRecordListModel() {
        return recordListModel;
    }


    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }


    @Override
    public int getType() {
        return VIEW_TYPE;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.record_list;
    }

    @Override
    public void releasemediaplayer() {
        if (null != player) {
            releaseMediaPlayer();
        }
    }



    public class ViewHolder extends FastAdapter.ViewHolder<RecordListAdapter> implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        private AppCompatImageView rate;
        private AppCompatTextView recordTitle;
        private AppCompatTextView recordTtime;
        private AppCompatTextView recordTime;
        private AppCompatTextView recordSize;
        private LinearLayoutCompat controllerLayout;
        private LinearLayoutCompat detailsLayout;
        private AppCompatTextView timeCurrent;
        private AppCompatTextView time;
        private AppCompatImageView play;
        private AppCompatImageView edit;
        private AppCompatImageView share;
        private AppCompatImageView delete;
        private RecordListAdapter currentItem;
        private SeekBar mediacontrollerProgress;
        private Handler mHandler = new Handler();
        private StringBuilder mFormatBuilder;
        private Formatter mFormatter;




        public ViewHolder(View itemView) {
            super(itemView);
            rate = (AppCompatImageView) itemView.findViewById(R.id.rate);
            recordTitle = (AppCompatTextView) itemView.findViewById(R.id.record_title);
            recordTtime = (AppCompatTextView) itemView.findViewById(R.id.record_ttime);
            recordTime = (AppCompatTextView) itemView.findViewById(R.id.record_time);
            recordSize = (AppCompatTextView) itemView.findViewById(R.id.record_size);
            controllerLayout = (LinearLayoutCompat) itemView.findViewById(R.id.controllerLayout);
            detailsLayout = (LinearLayoutCompat) itemView.findViewById(R.id.detailsLayout);
            timeCurrent = (AppCompatTextView) itemView.findViewById(R.id.time_current);
            mediacontrollerProgress = (SeekBar) itemView.findViewById(R.id.mediacontroller_progress);
            time = (AppCompatTextView) itemView.findViewById(R.id.time);
            play = (AppCompatImageView) itemView.findViewById(R.id.play);
            edit = (AppCompatImageView) itemView.findViewById(R.id.edit);
            share = (AppCompatImageView) itemView.findViewById(R.id.share);
            delete = (AppCompatImageView) itemView.findViewById(R.id.delete);
            mFormatBuilder = new StringBuilder();
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
            mediacontrollerProgress.setOnSeekBarChangeListener(this);
            play.setOnClickListener(this);
            detailsLayout.setOnClickListener(this);


        }

        @Override
        public void bindView(@NonNull RecordListAdapter item, @NonNull List<Object> payloads) {
            currentItem = item;
            recordTitle.setText(item.getRecordListModel().getTitle());
            recordTtime.setText(item.getRecordListModel().getLength());
            recordTime.setText(item.getRecordListModel().getCreatedtime());
            recordSize.setText(item.getRecordListModel().getFilesize());
            if (item.getRecordListModel().getRate()) {
                rate.setImageResource(R.drawable.ic_star);
            } else {
                rate.setImageResource(R.drawable.ic_empstar);
            }

            if (getAdapterPosition() == playingposition) {
                updateSeekBar();
            } else {
                updateNonPlayingView();
            }
        }



        @Override
        public void unbindView(@NonNull RecordListAdapter item) {
            recordTitle.setText(null);
            recordTtime.setText(null);
            recordTime.setText(null);
            recordSize.setText(null);
            timeCurrent.setText(null);
            time.setText(null);
            mediacontrollerProgress.setProgress(0);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.detailsLayout:
                    if (getAdapterPosition() == viewposition) {
                        controllerLayout.setVisibility(View.GONE);
                    }else{
                        controllerLayout.setVisibility(View.VISIBLE);
                        viewposition = getAdapterPosition();
                    }
                    break;
                case R.id.play:
                    if (getAdapterPosition() == playingposition) {
                        if (player.isPlaying()) {
                            play.setImageResource(R.drawable.ic_media_play);
                            player.pause();
                        } else {
                            play.setImageResource(R.drawable.ic_media_pause);
                            player.start();
                        }
                    } else {
                        playingposition = getAdapterPosition();
                        if (player != null) {
                            player.release();
                        }
                        play.setImageResource(R.drawable.ic_media_pause);
                        updateNonPlayingView();
                        playAudio();
                        updateSeekBar();
                    }
                    break;
            }
        }


        public void playAudio() {
            Uri trackUri = Uri.parse(currentItem.getRecordListModel().getFilepath());
            player = MediaPlayer.create(context, trackUri);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    updateNonPlayingView();
                    releaseMediaPlayer();
                }
            });
            player.start();
        }


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            timeCurrent.setText(stringForTime(progress));
            if (player != null && fromUser) {
                player.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(mUpdateTimeTask);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        private void updateSeekBar() {
            mediacontrollerProgress.setMax(player.getDuration());
            Toast.makeText(context,"hello",Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }

        private Runnable mUpdateTimeTask = new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    int currentPosition = player.getCurrentPosition();
                    int total = player.getDuration();
                    if (total != 0) {
                        time.setText(stringForTime(total));
                    }
                    if (currentPosition != 0) {
                        mediacontrollerProgress.setProgress(currentPosition);
                    }
                }
                mHandler.postDelayed(this, 100);
            }
        };

        private String stringForTime(int timeMs) {
            int totalSeconds = timeMs / 1000;
            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours = totalSeconds / 3600;
            mFormatBuilder.setLength(0);
            if (hours > 0) {
                return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            } else {
                return mFormatter.format("%02d:%02d", minutes, seconds).toString();
            }
        }

        private void updateNonPlayingView() {
                mHandler.removeCallbacks(mUpdateTimeTask);
                mediacontrollerProgress.setProgress(0);
        }

    }

    private void releaseMediaPlayer() {
        player.release();
        player = null;
        playingposition = -1;
    }



}
