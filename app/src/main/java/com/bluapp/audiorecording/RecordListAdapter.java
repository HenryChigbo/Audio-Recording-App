package com.bluapp.audiorecording;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.net.URLConnection;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class RecordListAdapter extends AbstractItem<RecordListAdapter, RecordListAdapter.ViewHolder> {


    private final int VIEW_TYPE = 2222;
    private RecordListModel recordListModel;
    private Context context;
    private MediaPlayer player;
    private int playingposition;
    private int viewposition = -1;
    private RecyclerSelectInterface recyclerSelectInterface;


    public RecordListAdapter(RecordListModel recordListModel, Context context, RecyclerSelectInterface recyclerSelectInterface) {
        this.context = context;
        this.recordListModel = recordListModel;
        this.playingposition = -1;
        this.recyclerSelectInterface = recyclerSelectInterface;
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
        private RecordDAO recordDAO;


        private ViewHolder(View itemView) {
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
            recordDAO = (RecordDAO) RecordDatabase.getInstance(context).recordDAO();
            mFormatBuilder = new StringBuilder();
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
            mediacontrollerProgress.setOnSeekBarChangeListener(this);
            rate.setOnClickListener(this);
            play.setOnClickListener(this);
            edit.setOnClickListener(this);
            share.setOnClickListener(this);
            delete.setOnClickListener(this);
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

            if (getAdapterPosition() != viewposition) {
                controllerLayout.setVisibility(View.GONE);
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
                case R.id.rate:
                    if(currentItem.getRecordListModel().getRate()){
                        recordDAO.updaterate(false, currentItem.getRecordListModel().getId());
                    }else{
                        recordDAO.updaterate(true, currentItem.getRecordListModel().getId());
                    }
                    break;
                case R.id.detailsLayout:
                    if (getAdapterPosition() == viewposition) {
                        controllerLayout.setVisibility(View.GONE);
                        viewposition = -1;
                    } else {
                        controllerLayout.setVisibility(View.VISIBLE);
                        viewposition = getAdapterPosition();
                    }
                    if (player != null && player.isPlaying()) {
                        releaseMediaPlayer();
                    }
                    recyclerSelectInterface.selectedrecord();
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
                case R.id.edit:
                    editDialog();
                    break;
                case R.id.share:
                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                    intentShareFile.setType(URLConnection.guessContentTypeFromName(currentItem.getRecordListModel().getTitle()));
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(currentItem.getRecordListModel().getFilepath()));
                    context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
                    break;
                case R.id.delete:
                    deleteDialog();
                    break;
            }
        }

        private void editDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Rename Recording");
            final EditText input = new EditText(context);
            String  resultName= currentItem.getRecordListModel().getTitle().replace(".mp3", "");
            input.setText(resultName);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setCancelable(true);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    String recordname = input.getText().toString();
                    if (recordname.equals("")) {
                        Toast.makeText(context, "Field is empty", Toast.LENGTH_LONG).show();
                    } else {
                        File file = new File(currentItem.getRecordListModel().getFilepath());
                        File absolutepath = new File(file.getAbsolutePath());
                        if (file.exists()) {
                            File oldfile = new File(currentItem.getRecordListModel().getFilepath());
                            File newfile = new File(absolutepath.getParent(),recordname+".mp3");
                            if(oldfile.renameTo(newfile)){
                                recordDAO.update(recordname+".mp3", newfile.toString(), currentItem.getRecordListModel().getId());
                            }
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        private void deleteDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Recording");
            builder.setMessage("...\\" + currentItem.getRecordListModel().getTitle() + "\n\n" + "Are you sure?");
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    File file = new File(currentItem.getRecordListModel().getFilepath());
                    if (file.exists()) {
                        if (file.delete()) {
                            recordDAO.delete(currentItem.getRecordListModel().getId());
                        }
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

        private void playAudio() {
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
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }

        private Runnable mUpdateTimeTask = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
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
