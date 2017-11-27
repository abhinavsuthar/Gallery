package com.developer.abhinav_suthar.gallery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.activities.Video1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


public class Main_VideoAdapter extends RecyclerView.Adapter<Main_VideoAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<HashMap<String, String>> VideoList = new ArrayList<>();

    public Main_VideoAdapter(Context context, ArrayList<HashMap<String, String>> VideoList){
        this.context = context;
        this.VideoList = VideoList;
    }

    @Override
    public Main_VideoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_0, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final Main_VideoAdapter.MyViewHolder holder, int position) {
        HashMap<String, String> photoAlbumDetail = VideoList.get(position);

        holder.albumArt.setImageResource(R.drawable.folder);
        holder.albumTitle.setText(photoAlbumDetail.get("key_album"));
        holder.albumCount.setText(photoAlbumDetail.get("key_countPhoto"));

        long millis = Long.parseLong(photoAlbumDetail.get("key_timestamp"))*1000L;
        Date d = new Date(millis);
        @SuppressLint("SimpleDateFormat") DateFormat df=new SimpleDateFormat("dd-MM-yyyy  HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        holder.albumModified.setText(df.format(d));

        holder.VdCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Video1.class);
                intent.putExtra("key_albumName", VideoList.get(holder.getAdapterPosition()).get("key_album"));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return VideoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView albumTitle, albumCount, albumModified;
        private CardView VdCardView;
        private ImageView albumArt;
        MyViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.txtVdFolderName);
            albumCount = itemView.findViewById(R.id.txtVdCount);
            albumModified = itemView.findViewById(R.id.txtVdModifiedDate);
            albumArt = itemView.findViewById(R.id.imgVdFolderIcon);

            VdCardView = itemView.findViewById(R.id.VdCardView);
        }
    }
}
