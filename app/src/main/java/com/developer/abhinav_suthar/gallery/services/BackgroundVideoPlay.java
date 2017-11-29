package com.developer.abhinav_suthar.gallery.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.activities.Video2;
import com.developer.abhinav_suthar.gallery.extras.Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundVideoPlay extends Service {

    private AudioManager audioManager;
    private static MediaPlayer mediaPlayer;
    private static ArrayList<HashMap<String,String>> mediaList;
    private int index;
    private static boolean isMusic = false, shuffle;
    private AudioManager.OnAudioFocusChangeListener listener;{
        listener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                if (mediaPlayer!=null){
                    switch (i) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            mediaPlayer.setVolume(1F,1F);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            mediaPlayer.pause();
                            pauseMediaPlayer();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mediaPlayer.setVolume(0.1F,0.1F);
                            break;
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        index = intent.getIntExtra("video_number",0);
        int videoPos = intent.getIntExtra("video_position", 0);
        isMusic = intent.getBooleanExtra("Audio", false);
        shuffle = intent.getBooleanExtra("shuffle", true);


        mediaList = (isMusic) ?  Utils.getMusicList() : Utils.getMediaList();

        playVideo();
        mediaPlayer.seekTo(videoPos);
        registerBroadcastReceiver();

        return START_NOT_STICKY;
    }

    private void playVideo(){



        try {
            mediaPlayer.setDataSource(mediaList.get(index).get("key_path"));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_SHORT).show();
            stopSelf();
        }




        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    mp.start();
                    showNotification();
                }else Toast.makeText(getApplicationContext(), "Can't play! Some other player is using audio!", Toast.LENGTH_SHORT).show();
            }
        });




        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (shuffle) index = new Random().nextInt(mediaList.size());
                if (index == mediaList.size()-1)
                    pauseMediaPlayer();
                else {
                    index++; mediaPlayer.reset(); playVideo();
                }
            }
        });


    }


    //--------------------------------------------------------------------------------------------//
    //All about showing notification
    private RemoteViews contentView;
    private Notification notification;
    private Timer vdNotiTimeTimer;
    private boolean updateNotificationTime = true;
    private void showNotification(){
        contentView = new RemoteViews(getPackageName(), R.layout.video_playback_noti);
        File f = new File(mediaList.get(index).get("key_path"));

        if (isMusic)
            musicAlbumArt();
        else{
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mediaList.get(index).get("key_path"), MediaStore.Video.Thumbnails.MICRO_KIND);
            contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
        }

        contentView.setImageViewResource(R.id.noti_vd_prev, R.drawable.ic_previous);
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_pause);
        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_next);
        contentView.setTextViewText(R.id.noti_title, f.getName());

        Intent intent = new Intent(this, Video2.class);
        intent.setAction("notificationVideoAction");
        intent.putExtra("key_position", index);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent playVideo = PendingIntent.getActivity(this,500,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        if (isMusic) playVideo = null;

        Notification.Builder mBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.launcher)
                .setContent(contentView)
                .setContentIntent(playVideo);

        Intent prevIntent = new Intent();
        prevIntent.setAction("prev");
        PendingIntent pprevIntent = PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent();
        playIntent.setAction("play");
        PendingIntent pplayIntent = PendingIntent.getBroadcast(this,1,playIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent();
        nextIntent.setAction("next");
        PendingIntent pnextIntent = PendingIntent.getBroadcast(this,2,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent();
        deleteIntent.setAction("stopService");
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(this,3,deleteIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        contentView.setOnClickPendingIntent(R.id.noti_vd_prev, pprevIntent);
        contentView.setOnClickPendingIntent(R.id.noti_vd_play, pplayIntent);
        contentView.setOnClickPendingIntent(R.id.noti_vd_next, pnextIntent);
        mBuilder.setDeleteIntent(pDeleteIntent);

        notification = mBuilder.build();
        notification.flags |= Notification.DEFAULT_LIGHTS;

        startForeground(5198, notification);
        notificationTime();
    }
    private void notificationTime(){
        try {
            vdNotiTimeTimer.cancel();
        } catch (Exception ignored) {}


        vdNotiTimeTimer = new Timer();
        long duration = mediaPlayer.getCurrentPosition();
        final Date d2 = new Date(mediaPlayer.getDuration());
        duration = duration/1000L;
        final DateFormat df;
        if (duration<3600) df = new SimpleDateFormat("mm:ss");
        else df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        vdNotiTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (updateNotificationTime){
                    int position = 0;

                    try { position = mediaPlayer.getCurrentPosition();
                    } catch (Exception ignored) {}

                    Date d = new Date(position);
                    contentView.setTextViewText(R.id.noti_vd_time, df.format(d)+"/"+df.format(d2));
                    startForeground(5198, notification);
                }
            }
        },0,1000);
    }
    private void musicAlbumArt(){
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(mediaList.get(index).get("key_path"));
            byte [] data = mmr.getEmbeddedPicture();
            if (data==null) contentView.setImageViewResource(R.id.noti_album_art, R.drawable.ic_audio_bg);
            else {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
            }
        } catch (IllegalArgumentException e) {
            contentView.setImageViewResource(R.id.noti_album_art, R.drawable.ic_audio_bg);
        }
    }
    private void pauseMediaPlayer(){
        audioManager.abandonAudioFocus(listener);
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_play);
        startForeground(5198, notification);
        updateNotificationTime = false;
        stopForeground(false);
    }



    public static MediaPlayer getMediaPlayer(){return  mediaPlayer;}
    public static int stopVideo(){
        try {
            if (mediaPlayer!=null){
                int videoPos = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
                mediaPlayer.release();
                if (!isMusic) Utils.setMediaList(mediaList);
                return videoPos;
            }else return 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }
    @Override
    public void onDestroy() {
        stopVideo();
        try {
            vdNotiTimeTimer.cancel();
            unregisterReceiver(handler);
            audioManager.abandonAudioFocus(listener);
            stopForeground(true);
        } catch (Exception ignored) {}
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    //Notification click event handler
    NotificationActionHandler handler = new NotificationActionHandler();
    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction("prev");
        filter.addAction("play");
        filter.addAction("next");
        filter.addAction("stopService");
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(handler, filter);
    }
    private class NotificationActionHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction()==null) return;
            switch (intent.getAction()){



                case "prev":
                    if (mediaPlayer.getCurrentPosition()>7000)
                        mediaPlayer.seekTo(0);
                    else if (index!=1){
                        index--;
                        audioManager.abandonAudioFocus(listener);
                        mediaPlayer.reset(); playVideo();
                        updateNotificationTime = true;
                    }break;



                case "play":
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        pauseMediaPlayer();
                    }
                    else {
                        int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                            mediaPlayer.start();
                            contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_pause);
                            startForeground(5198, notification);
                            updateNotificationTime = true;
                        }else Toast.makeText(getApplicationContext(), "Can't play! Some other player is using audio!", Toast.LENGTH_SHORT).show();
                    }break;



                case "next":
                    if (shuffle) index = new Random().nextInt(mediaList.size());
                    if (index == mediaList.size()-1)
                        pauseMediaPlayer();
                    else {
                        index++; mediaPlayer.reset(); playVideo();
                        updateNotificationTime = true;
                    } break;



                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    mediaPlayer.pause();
                    pauseMediaPlayer();
                    break;


                case "stopService":
                    stopSelf();
                    break;


                default:
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
