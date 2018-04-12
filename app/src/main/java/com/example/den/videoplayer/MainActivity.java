package com.example.den.videoplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements SelectedVideoInterface {
    private List<String> list;
    private List<String> listName;
    private VideoView videoView;
    private TextView txt_ct, txt_td;
    private SeekBar seekBar;
    private Handler threadHandler = new Handler();
    private int timeDuration;
    private int curPosition;
    //    private Resources  res = this.getResources();//доступ к ресерсам;
    private static final int REQUEST_PERMITIONS = 1100;
    private ImageButton btn_play;
    private ImageButton btn_pause;
    private ImageButton btn_fwd;
    private ImageButton btn_rev;
    private ImageButton btn_next;
    private ImageButton btn_prev;
    private ImageButton btn_stop;
    private ImageButton btn_settings;
    private ImageButton btn_back;
    private Button buttonPermis;
    private int curTrackIndex;
    private boolean flagStartPlay = true;
    private boolean flagSavedInstanceState = false;
    private Runnable hideControls;
    private ControlsMode controlsState;
    private LinearLayout root;
    private LinearLayout top_controls;
    private LinearLayout middle_panel;
    private LinearLayout unlock_panel;
    private RelativeLayout layoutButtonPermiss;
    private View decorView;
    private View view;
    private int immersiveOptions;
    private TextView txt_title;
    private DialogPlayList dialogPlayList;

    public enum ControlsMode {
        LOCK, FULLCONTORLS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);

        buttonPermis = findViewById(R.id.idButtonPermission);
        layoutButtonPermiss = findViewById(R.id.idLayoutButtonPermiss);
        buttonPermis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.getListVodeoWithPermissionCheck(MainActivity.this);
            }
        });

        if (savedInstanceState != null) {
            curTrackIndex = savedInstanceState.getInt("curTrackIndex");
            list = savedInstanceState.getStringArrayList("list");
            listName = savedInstanceState.getStringArrayList("listName");
            curPosition = savedInstanceState.getInt("curPosition");
            flagStartPlay = savedInstanceState.getBoolean("flagStartPlay");
            flagSavedInstanceState = true;

            if (list == null){
                Snackbar.make(view, "Нельзя запустить плеер без разрешений!", Snackbar.LENGTH_LONG).show();
            } else if (list.size() == 0) {
                buttonPermis.setVisibility(View.GONE);
                Snackbar.make(view, "На устройстве отсутствует видео файлы", Snackbar.LENGTH_LONG).show();
            } else {
                instalVidget();
                installVideo();
                videoView.seekTo(curPosition);
                initializationButtons();
            }
        } else MainActivityPermissionsDispatcher.getListVodeoWithPermissionCheck(this);
        if (dialogPlayList != null && !dialogPlayList.isVisible()) {
            flagStartPlay = true;
        }
    }//onCreate

    private void instalVidget() {
        txt_title = findViewById(R.id.txt_title);
        btn_back = findViewById(R.id.btn_back);
        btn_play = findViewById(R.id.btn_play);
        btn_pause = findViewById(R.id.btn_pause);
        btn_fwd = findViewById(R.id.btn_fwd);
        btn_rev = findViewById(R.id.btn_rev);
        btn_prev = findViewById(R.id.btn_prev);
        btn_next = findViewById(R.id.btn_next);
        btn_stop = findViewById(R.id.btn_stop);
        btn_settings = findViewById(R.id.btn_settings);
        txt_ct = findViewById(R.id.txt_currentTime);
        txt_td = findViewById(R.id.txt_totalDuration);
        seekBar = findViewById(R.id.seekbar);

        root = findViewById(R.id.root);
        root.setVisibility(View.VISIBLE);
        LinearLayout seekbar_time = findViewById(R.id.seekbar_time);
        seekbar_time.setVisibility(View.VISIBLE);
        LinearLayout top = findViewById(R.id.top);
        top.setVisibility(View.VISIBLE);
        LinearLayout bottom_controls = findViewById(R.id.controls);
        bottom_controls.setVisibility(View.VISIBLE);

        immersiveOptions = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(immersiveOptions);
    }//instalVidget

    private void initializationButtons() {
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
        btn_back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (list != null) {
                            videoView.stopPlayback();//mMediaPlayer = null
                        }
                        finish();
                    }
                });
        btn_play.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!videoView.isPlaying()) {
                            flagStartPlay = true;
                            installVideo();
                            changePausePlay();
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
                        if (curTrackIndex != 0) {
                            curTrackIndex -= 1;
                            curPosition = 0;
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
                        if (curTrackIndex != list.size() - 1) {
                            curTrackIndex += 1;
                            curPosition = 0;
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
                        dialogPlayList = new DialogPlayList();
                        Bundle args = new Bundle();//создаем Bundle для передачи в диалог информации
                        args.putStringArrayList("listName", (ArrayList<String>) listName);
                        dialogPlayList.setArguments(args);//показать данные в диалоге
                        dialogPlayList.show(getSupportFragmentManager(), "dialogPlayList");// отображение диалогового окна в фрагменте
                        if (videoView.isPlaying()) {
                            videoView.pause();
                            btn_pause.setVisibility(View.GONE);
                            btn_play.setVisibility(View.VISIBLE);
                            flagStartPlay = false;
                            curPosition = videoView.getCurrentPosition();
                        }
                    }
                });
    }//initializationButtons

    private void changePausePlay() {
        btn_pause.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.GONE);
    }//changePausePlay

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
        decorView.setSystemUiVisibility(immersiveOptions);
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
        videoView = findViewById(R.id.idVideoView);
        if (list != null) {
            if (list.size() != 0) {
                hideControls = new Runnable() {
                    @Override
                    public void run() {
                        hideAllControls();
                    }
                };
                String videoSource = list.get(curTrackIndex);
                videoView.setVideoPath(videoSource);
                videoView.requestFocus(0);
                if (flagStartPlay) {
                    videoView.seekTo(curPosition);
                    videoView.start(); // начинаем воспроизведение автоматически
                    flagStartPlay = true;
                } else {
                    videoView.pause();
                    btn_pause.setVisibility(View.GONE);
                    btn_play.setVisibility(View.VISIBLE);
                }

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        timeDuration = videoView.getDuration();
                        String time = millisecondsToString(timeDuration);
                        txt_td.setText(time);
                        seekBar.setMax(timeDuration);
                        txt_title.setText(listName.get(curTrackIndex));
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
                UpdateSeekBar updateSeekBar = new UpdateSeekBar();
                threadHandler.postDelayed(updateSeekBar, 50);
                threadHandler.postDelayed(hideControls, 3000);
                controlsState = ControlsMode.FULLCONTORLS;
            }
        }
    }

    @Override
    public void selectVideo(int position) {
        flagStartPlay = true;
        curTrackIndex = position;
        curPosition = 0;
        installVideo();
    }

    class UpdateSeekBar implements Runnable {
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

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getListVodeo() {
        layoutButtonPermiss = findViewById(R.id.idLayoutButtonPermiss);
        layoutButtonPermiss.setVisibility(View.GONE);
        list = new ArrayList<>();
        listName = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_LONG).show();
            return;
        } else if (!cursor.moveToFirst()) {
            Snackbar.make(view, "На устройстве отсутствует видео файлы", Snackbar.LENGTH_INDEFINITE).show();
            return;
        } else {
            instalVidget();
            int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int dataColumnName = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            do {
                String name = cursor.getString(dataColumnName);
                if (name != null) listName.add(name);
                list.add(cursor.getString(dataColumn));
            } while (cursor.moveToNext());
        }
        cursor.close();
        curTrackIndex = 0;
        installVideo();
        if (flagSavedInstanceState) videoView.seekTo(curPosition);
        initializationButtons();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curTrackIndex", curTrackIndex);
        outState.putStringArrayList("list", (ArrayList<String>) list);
        outState.putStringArrayList("listName", (ArrayList<String>) listName);
        outState.putInt("curPosition", curPosition);
        outState.putBoolean("flagStartPlay", flagStartPlay);
    }

    //===========================================================================
    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void permissionsDenied() {
        Snackbar.make(view, "Нельзя запустить плеер без разрешений!", Snackbar.LENGTH_LONG).show();
    }//permissionsDenied

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Получите разрешения!")
                .setMessage("Нельзя запустить плеер без разрешений!")
                .setPositiveButton("Хорошо", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Не хочу", (dialog, which) -> dialog.dismiss()).create()
                .show();
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Получите разрешения!")
                .setMessage("Необходимо получить разрешения для доступа к списку видеофайлов")
                .setPositiveButton("Хорошо", (dialog, button) -> request.proceed())
                .setNegativeButton("Не хочу", (dialog, button) -> request.cancel())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }//onBackPressed
}
