package com.example.den.videoplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DeleteUserInterface{
    private List<String> list;
    private List<String> listName;
    private VideoView videoView;
    private TextView txt_ct, txt_td;
    private SeekBar seekBar;
    private Handler threadHandler = new Handler();
    private int timeDuration;
    private int curPosition;
    private ImageButton btn_play;
    private ImageButton btn_pause;
    private ImageButton btn_fwd;
    private ImageButton btn_rev;
    private ImageButton btn_next;
    private ImageButton btn_prev;
    private ImageButton btn_stop;
    private ImageButton btn_settings;
    private ImageButton btn_back;
    private int currentTrackIndex;
    private Runnable hideControls;
    private ControlsMode controlsState;
    private LinearLayout root, top_controls, middle_panel, unlock_panel, bottom_controls;
    private View decorView;
    private int uiImmersiveOptions;
    private TextView txt_title;
    public enum ControlsMode {
        LOCK, FULLCONTORLS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_title = (TextView) findViewById(R.id.txt_title);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_pause = (ImageButton) findViewById(R.id.btn_pause);
        btn_fwd = (ImageButton) findViewById(R.id.btn_fwd);
        btn_rev = (ImageButton) findViewById(R.id.btn_rev);
        btn_prev = (ImageButton) findViewById(R.id.btn_prev);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_stop = (ImageButton) findViewById(R.id.btn_stop);
        btn_settings = (ImageButton) findViewById(R.id.btn_settings);

        root = (LinearLayout) findViewById(R.id.root);
        root.setVisibility(View.VISIBLE);

        uiImmersiveOptions = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiImmersiveOptions);

        if (savedInstanceState != null) {
            currentTrackIndex = savedInstanceState.getInt("currentTrackIndex");
            list = savedInstanceState.getStringArrayList("list");
            listName = savedInstanceState.getStringArrayList("listName");
            curPosition = savedInstanceState.getInt("curPosition");
        }

        if (list == null) getListVodeo();
        installVideo();
        if (savedInstanceState != null)videoView.seekTo(curPosition);
        initializationButtons();

        txt_ct = findViewById(R.id.txt_currentTime);
        txt_td = findViewById(R.id.txt_totalDuration);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
            }
        });
    }

    private void initializationButtons() {

        btn_back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.stopPlayback();//mMediaPlayer = null
                        finish();
                    }
                });
        btn_play.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!videoView.isPlaying()) {
                            installVideo();
                            btn_pause.setVisibility(View.VISIBLE);
                            btn_play.setVisibility(View.GONE);
                        }
                    }
                });
        btn_pause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (videoView.isPlaying()) {
                            videoView.pause();
                            btn_pause.setVisibility(View.GONE);
                            btn_play.setVisibility(View.VISIBLE);
                        }
                    }
                });
        btn_fwd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.seekTo(curPosition + 5000);
                    }
                });
        btn_rev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.seekTo(curPosition - 5000);
                    }
                });
        btn_prev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.suspend();//MediaPlayer !=null
                        if (currentTrackIndex != 0) {
                            currentTrackIndex -=1;
                            installVideo();
                            changePausePlay();
                        } else installVideo();
                    }
                });
        btn_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.suspend();//MediaPlayer !=null
                        if (currentTrackIndex != list.size() - 1) {
                            currentTrackIndex +=1;
                            installVideo();
                            changePausePlay();
                        } else installVideo();
                    }
                });
        btn_stop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.stopPlayback();//mMediaPlayer = null
                        btn_pause.setVisibility(View.GONE);
                        btn_play.setVisibility(View.VISIBLE);
                    }
                });
        btn_settings.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogReferenceCar dialogReferenceCar = new DialogReferenceCar();
                        dialogReferenceCar.show(getSupportFragmentManager(), "dialogReferenceCar");// отображение диалогового окна в фрагменте
                        if (videoView.isPlaying()) {
                            videoView.pause();
                            btn_pause.setVisibility(View.GONE);
                            btn_play.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }//initializationButtons

    private void changePausePlay(){
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);
    }
//========================================================================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                showControls();
                break;
        }
        return super.onTouchEvent(event);
    }

    {
        hideControls = new Runnable() {
            @Override
            public void run() {
                hideAllControls();
            }
        };
    }

    private void hideAllControls() {
        if (controlsState == ControlsMode.FULLCONTORLS) {
            if (root.getVisibility() == View.VISIBLE) {
                root.setVisibility(View.GONE);
            }
        } else if (controlsState == ControlsMode.LOCK) {
            if (unlock_panel.getVisibility() == View.VISIBLE) {
                unlock_panel.setVisibility(View.GONE);
            }
        }
        decorView.setSystemUiVisibility(uiImmersiveOptions);
    }

    private void showControls() {
        if (controlsState == ControlsMode.FULLCONTORLS) {
            if (root.getVisibility() == View.GONE) {
                root.setVisibility(View.VISIBLE);
            }
        } else if (controlsState == ControlsMode.LOCK) {
            if (unlock_panel.getVisibility() == View.GONE) {
                unlock_panel.setVisibility(View.VISIBLE);
            }
        }
        threadHandler.removeCallbacks(hideControls);
        threadHandler.postDelayed(hideControls, 3000);
    }
   // ==============================================================================================

    private void installVideo() {
        String videoSource = list.get(currentTrackIndex);
        videoView = findViewById(R.id.idVideoView);
        videoView.setVideoPath(videoSource);
        videoView.requestFocus(0);
        videoView.start(); // начинаем воспроизведение автоматически
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                timeDuration = videoView.getDuration();
                String time = millisecondsToString(timeDuration);
                txt_td.setText(time);
                seekBar.setMax(timeDuration);
                txt_title.setText(listName.get(currentTrackIndex));
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.stopPlayback();//mMediaPlayer = null
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
            }
        });

        UpdateSeekBarThread updateSeekBarThread = new UpdateSeekBarThread();
        threadHandler.postDelayed(updateSeekBarThread, 50);
        threadHandler.postDelayed(hideControls, 3000);
        controlsState = ControlsMode.FULLCONTORLS;
    }

    @Override
    public void deleteUser(int position) {
        currentTrackIndex = position;
        installVideo();
    }

    class UpdateSeekBarThread implements Runnable {
        public void run() {
            curPosition = videoView.getCurrentPosition();
            txt_ct.setText(millisecondsToString(curPosition));
            seekBar.setProgress(curPosition);
            threadHandler.postDelayed(this, 200);
        }
    }

    private String millisecondsToString(int milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        if (TimeUnit.MILLISECONDS.toHours(timeDuration) == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void getListVodeo() {
        list = new ArrayList<>();
        listName = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_LONG).show();
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(this, "На устройстве отсутствует видео файлы", Toast.LENGTH_LONG).show();
        } else {
            int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int dataColumnName = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            do {
                list.add(cursor.getString(dataColumn));
                listName.add(cursor.getString(dataColumnName));
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        currentTrackIndex = 0;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTrackIndex", currentTrackIndex);
        outState.putStringArrayList("list", (ArrayList<String>) list);
        outState.putStringArrayList("listName", (ArrayList<String>) listName);
        outState.putInt("curPosition",  curPosition);

    }
}
