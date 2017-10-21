package com.developer.abhinav_suthar.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.developer.abhinav_suthar.gallery.extras.BackgroundVideoPlay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class Video2 extends AppCompatActivity{

    private VideoView videoView;
    private FrameLayout layout;
    boolean backPlay = true, autoPlay = true;
    private ArrayList<HashMap<String,String>> videoList;
    private int p, seekTo=0;
    private MediaPlayer mp;
    private AudioManager am;
    private Timer vdCurrTime, t = new Timer();
    private AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            switch (i) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    try {
                        mp.setVolume(1F, 1F);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        mp.pause();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    am.abandonAudioFocus(listener);
                    ((ImageButton)findViewById(R.id.v2pause)).setImageResource(android.R.drawable.ic_media_play);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    try {
                        mp.setVolume(0.1F, 0.1F);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_2);

        seekTo = BackgroundVideoPlay.stopVideo();
        stopService(new Intent(this, BackgroundVideoPlay.class));

        if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
            if (getIntent().getType().startsWith("video/")) {
                Uri videoUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                HashMap<String, String> temp = new HashMap<>();
                temp.put("key_path", getRealPathFromURI(this, videoUri));
                videoList = new ArrayList<>();
                videoList.clear();
                videoList.add(temp);
                seekTo=0;
            }
        }else if("notificationVideoAction".equals(getIntent().getAction())){
            p = getIntent().getIntExtra("key_position",0);
            videoList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("key_list");
        }else {
            videoList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("key_list");
            p = getIntent().getIntExtra("key_pos", 0);
            seekTo=0;
        }

        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        videoView = findViewById(R.id.videoView);
        layout = findViewById(R.id.video_2_layout);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        autoPlay = sp.getBoolean("key_autoPlay", true);
        backPlay = sp.getBoolean("key_backPlay", false);

        playVideo();
        registerBroadcastReceiver();
        actionBarController();
    }

    AudioBecomingNoisy handler = new AudioBecomingNoisy();
    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(handler, filter);
    }

    private void playVideo(){

        videoView.setVideoPath(videoList.get(p).get("key_path"));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp =  mediaPlayer;
                int result = am.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    mediaPlayer.start();
                    mediaPlayer.seekTo(seekTo);
                    seekTo=0;
                    fixVideoOrientation();
                    mediaController();
                } else Toast.makeText(Video2.this, "Another application is using audio", Toast.LENGTH_SHORT).show();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                am.abandonAudioFocus(listener);
                if (p==videoList.size()-1 || !autoPlay) {finish(); return;}
                p++;
                playVideo();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Video2.this);
                builder.setCancelable(false);
                builder.setMessage("Can't play this video ! ! !")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    am.abandonAudioFocus(listener);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Video2.super.onBackPressed();
                                overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
                            }
                        });
                builder.create().show();
                return true;
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideShowMediaController();
            }
        });
    }

    private void mediaController(){

        try {
            vdCurrTime.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageButton prev = findViewById(R.id.v2prev);
        ImageButton reww = findViewById(R.id.v2rew);
        final ImageButton play = findViewById(R.id.v2pause);
        ImageButton ffwd = findViewById(R.id.v2ffwd);
        ImageButton next = findViewById(R.id.v2next);

        final TextView curTime = findViewById(R.id.v2time_current);
        TextView endTime = findViewById(R.id.v2time);

        final SeekBar seekBar  = findViewById(R.id.v2mediacontroller_progress);
        seekBar.setMax(mp.getDuration());

        vdCurrTime = new Timer();
        long duration = mp.getDuration();
        Date d = new Date(duration);
        duration = duration/1000L;
        final DateFormat df;
        if (duration<3600) df = new SimpleDateFormat("mm:ss");
        else df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        endTime.setText(df.format(d));
        vdCurrTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int duration=0;
                        try {
                            duration = mp.getCurrentPosition();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Date d = new Date(duration);
                        curTime.setText(df.format(d));

                        final int finalDuration = duration;
                        seekBar.post(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(finalDuration);
                            }
                        });
                    }
                });
            }
        },0,1000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mp.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p==0) return;
                am.abandonAudioFocus(listener);
                p--;
                vdCurrTime.cancel();
                playVideo();
            }
        });
        reww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.seekTo(mp.getCurrentPosition()-5000);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    am.abandonAudioFocus(listener);
                    play.setImageResource(android.R.drawable.ic_media_play);
                }
                else {
                    int result = am.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                        mp.start();
                        play.setImageResource(android.R.drawable.ic_media_pause);
                    } else Toast.makeText(Video2.this, "Another application is using audio", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ffwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.seekTo(mp.getCurrentPosition()+15000);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p==videoList.size()-1) return;
                am.abandonAudioFocus(listener);
                p++;
                vdCurrTime.cancel();
                playVideo();
            }
        });

        ((TextView) findViewById(R.id.v2txtVdName)).setText(videoList.get(p).get("key_displayName"));
    }

    private void hideShowMediaController(){
        final LinearLayout top    = findViewById(R.id.v2TopLayout);
        final LinearLayout bottom = findViewById(R.id.v2BottomLayout);


        if (top.getVisibility()==View.VISIBLE){

            top.setAlpha(1.0f);
            top.animate()
                    .translationY(-top.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            top.setVisibility(View.GONE);
                        }
                    });
            bottom.setAlpha(1.0f);
            bottom.animate()
                    .translationY(bottom.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bottom.setVisibility(View.GONE);
                        }
                    });

            t.cancel();
            t = new Timer();
        }else {
            top.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.VISIBLE);

            top.setAlpha(0.0f);
            top.animate()
                    .translationYBy(top.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
            bottom.setAlpha(0.0f);
            bottom.animate()
                    .translationYBy(-bottom.getHeight())
                    .alpha(1.0f)
                    .setListener(null);

            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideShowMediaController();
                        }
                    });
                }
            }, 5000);
        }
    }

    private void actionBarController(){
        findViewById(R.id.v2imgBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Video2.super.onBackPressed();
                overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
            }
        });
        findViewById(R.id.v2imgBackgroundPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(Video2.this, BackgroundVideoPlay.class));
                Intent intent = new Intent(Video2.this, BackgroundVideoPlay.class);
                intent.putExtra("video_list", videoList);
                intent.putExtra("video_number",p);
                intent.putExtra("video_position", videoView.getCurrentPosition());
                startService(intent);
                backPlay = false;
                try {
                    videoView.pause();
                    am.abandonAudioFocus(listener);
                    unregisterReceiver(handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Video2.super.onBackPressed();
                overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) fixVideoOrientation();
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) fixVideoOrientation();
        mediaController();
    }

    private void fixVideoOrientation(){
        //VideoResolution
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoList.get(p).get("key_path"));
        Bitmap bmp = retriever.getFrameAtTime();
        int height=bmp.getHeight();
        int width=bmp.getWidth();
        float videoProportion = (float) width/(float) height;

        //Current screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width2  = displayMetrics.widthPixels;
        int height2 = displayMetrics.heightPixels;
        float screenProportion = (float) width2/(float) height2;


        if (videoProportion > screenProportion){

            ViewGroup.LayoutParams params = videoView.getLayoutParams();
            params.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = (int) ((float) width2 / videoProportion);
            videoView.setLayoutParams(params);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                videoView.setForegroundGravity(Gravity.CENTER);
            }*/

        }else {

            ViewGroup.LayoutParams params = videoView.getLayoutParams();
            params.width  = (int) (videoProportion * (float) height2);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoView.setLayoutParams(params);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                videoView.setForegroundGravity(Gravity.CENTER);
            }*/
        }
    }

    @Override
    public void onBackPressed() {
        if (backPlay&&videoView.isPlaying()){
            stopService(new Intent(Video2.this, BackgroundVideoPlay.class));
            Intent intent = new Intent(Video2.this, BackgroundVideoPlay.class);
            intent.putExtra("video_list", videoList);
            intent.putExtra("video_number",p);
            intent.putExtra("video_position", videoView.getCurrentPosition());
            startService(intent);
            backPlay = false;
        }
        try {
            videoView.pause();
            am.abandonAudioFocus(listener);
            unregisterReceiver(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
    }

    @Override
    protected void onStop() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mpPlaying = false;
        try {
            mpPlaying = mp.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((!sp.getBoolean("key_offScreen", false)) && mpPlaying) mp.pause();
        super.onStop();
    }

    @Override
    protected void onPause() {
        vdCurrTime.cancel();
        super.onPause();
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Video.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class AudioBecomingNoisy extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                mp.pause();
            }
        }
    }
}