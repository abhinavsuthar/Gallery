package com.developer.abhinav_suthar.gallery.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.fragments.Music;
import com.developer.abhinav_suthar.gallery.services.BackgroundVideoPlay;

import java.util.ArrayList;
import java.util.HashMap;


public class Main_AudioAdapter extends RecyclerView.Adapter<Main_AudioAdapter.MyViewHolder>{


    private Context context;
    private ArrayList<HashMap<String, String>> AudioList = new ArrayList<>();

    public Main_AudioAdapter(Context context, ArrayList<HashMap<String, String>> AudioList){
        this.context = context;
        this.AudioList = AudioList;
    }

    @Override
    public Main_AudioAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_0, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final Main_AudioAdapter.MyViewHolder holder, int position) {

        holder.title.setText(AudioList.get(holder.getAdapterPosition()).get("key_title"));
        holder.artist.setText(AudioList.get(holder.getAdapterPosition()).get("key_artist"));
        holder.albumName.setText(AudioList.get(holder.getAdapterPosition()).get("key_album"));

        Drawable background = holder.albumArt.getDrawable();
        if(background == null) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(AudioList.get(holder.getAdapterPosition()).get("key_path"));
                byte [] data = mmr.getEmbeddedPicture();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.albumArt.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                holder.albumArt.setImageResource(R.drawable.ic_audio_bg);
            }

        }

        /*long millis = Long.parseLong(photoAlbumDetail.get("key_duration"))*1000L;
        Date d = new Date(millis);
        @SuppressLint("SimpleDateFormat")
        DateFormat df=new SimpleDateFormat("dd-MM-yyyy  HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        holder.albumModified.setText(df.format(d));*/

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                context.stopService(new Intent(context, BackgroundVideoPlay.class));
                Intent intent = new Intent(context, BackgroundVideoPlay.class);
                intent.putExtra("video_number",holder.getAdapterPosition());
                intent.putExtra("Audio", true);
                context.startService(intent);

                //Music.musicPlayer = BackgroundVideoPlay.getMediaPlayer();

            }
        });
    }

    @Override
    public int getItemCount() {
        return AudioList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView title, artist, albumName;
        private ImageView albumArt;
        private RelativeLayout view;
        private MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtAdTitle);
            artist = itemView.findViewById(R.id.txtAdArtist);
            albumArt = itemView.findViewById(R.id.imgAdAlbumArt);
            albumName = itemView.findViewById(R.id.txtAdAlbum);

            view = itemView.findViewById(R.id.ad_baseRelativeLayout);

        }
    }
}
