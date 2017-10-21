package com.developer.abhinav_suthar.gallery.extras;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.Video2;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundVideoPlay extends Service {
    private AudioManager audioManager;
    private ArrayList<HashMap<String,String>> videoList;
    private int p,videoPos;
    private static MediaPlayer m;
    private AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            if (m!=null){
                switch (i) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        m.setVolume(1F,1F);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        m.pause();
                        audioManager.abandonAudioFocus(listener);
                        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_play);
                        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_clear);
                        startForeground(5198, notification);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        m.setVolume(0.1F,0.1F);
                        break;
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        videoList = (ArrayList<HashMap<String,String>>) intent.getSerializableExtra("video_list");
        p = intent.getIntExtra("video_number",0);
        videoPos = intent.getIntExtra("video_position",0);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            registerBroadcastReceiver();
            playVideo();
        }else Toast.makeText(getApplicationContext(), "No audio focus", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }

    private void playVideo(){
        m = new MediaPlayer();
        try {
            m.setDataSource(videoList.get(p).get("key_path"));
            m.prepare();
            m.seekTo(videoPos);
            videoPos=0;
            m.start();
            showNotification();
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    p++;
                    if (p==videoList.size()) {
                        p--;
                        m.pause();
                        m.seekTo(0);
                        audioManager.abandonAudioFocus(listener);
                        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_play);
                        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_clear);
                        startForeground(5198, notification);
                    }else{
                        m.stop();
                        m.release();
                        m=null;
                        playVideo();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            m.stop();
            m.release();
            m=null;
            stopSelf();
        }
    }

    private RemoteViews contentView;
    private Notification notification;
    private void showNotification(){
        contentView = new RemoteViews(getPackageName(), R.layout.video_playback_noti);
        File f = new File(videoList.get(p).get("key_path"));
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoList.get(p).get("key_path"), MediaStore.Video.Thumbnails.MICRO_KIND);
        contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
        contentView.setImageViewResource(R.id.noti_vd_prev, R.drawable.ic_previous);
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_pause);
        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_next);
        contentView.setTextViewText(R.id.noti_title, f.getName());

        Intent intent = new Intent(this, Video2.class);
        intent.setAction("notificationVideoAction");
        intent.putExtra("key_list", videoList);
        intent.putExtra("key_position", p);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent playVideo = PendingIntent.getActivity(this,500,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.whatsapp_icon)
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

        contentView.setOnClickPendingIntent(R.id.noti_vd_prev, pprevIntent);
        contentView.setOnClickPendingIntent(R.id.noti_vd_play, pplayIntent);
        contentView.setOnClickPendingIntent(R.id.noti_vd_next, pnextIntent);

        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.DEFAULT_LIGHTS;

        startForeground(5198, notification);
        notificationTime();
    }

    private Timer vdNotiTimeTimer;
    private void notificationTime(){
        try {
            vdNotiTimeTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        vdNotiTimeTimer = new Timer();
        long duration = m.getCurrentPosition();
        final Date d2 = new Date(m.getDuration());
        duration = duration/1000L;
        final DateFormat df;
        if (duration<3600) df = new SimpleDateFormat("mm:ss");
        else df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        vdNotiTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int position = 0;
                try {
                    position = m.getCurrentPosition();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Date d = new Date(position);
                contentView.setTextViewText(R.id.noti_vd_time, df.format(d)+"/"+df.format(d2));
                startForeground(5198, notification);
            }
        },0,1000);
    }

    NotificationActionHandler handler = new NotificationActionHandler();
    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction("prev");
        filter.addAction("play");
        filter.addAction("next");
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(handler, filter);
    }

    public static int stopVideo(){
        try {
            if (m!=null){
                int videoPos = m.getCurrentPosition();
                m.stop();
                m.release();
                m=null;
                return videoPos;
            }else return 0;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        try {
            stopVideo();
            vdNotiTimeTimer.cancel();
            unregisterReceiver(handler);
            audioManager.abandonAudioFocus(listener);
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private class NotificationActionHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "prev":
                    if (m.getCurrentPosition()>7000){
                        m.seekTo(0);
                    }else {
                        m.stop();
                        m.release();
                        m=null;
                        p--;
                        if (p<0) p++;
                        audioManager.abandonAudioFocus(listener);
                        audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        playVideo();
                    }
                    break;
                case "play":
                    if (m.isPlaying()) {
                        m.pause();
                        audioManager.abandonAudioFocus(listener);
                        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_play);
                        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_clear);
                        startForeground(5198, notification);
                    }
                    else {
                        int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                            m.start();
                            contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_pause);
                            contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_next);
                            startForeground(5198, notification);
                        }else Toast.makeText(getApplicationContext(), "Another application is using audio", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "next":
                    if(!m.isPlaying()) stopSelf();
                    else {
                        m.stop();
                        m.release();
                        m=null;
                        p++;
                        if (p==videoList.size()) p--;
                        audioManager.abandonAudioFocus(listener);
                        audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        playVideo();
                    }
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    m.pause();
                    audioManager.abandonAudioFocus(listener);
                    contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.ic_play);
                    contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.ic_clear);
                    startForeground(5198, notification);
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
