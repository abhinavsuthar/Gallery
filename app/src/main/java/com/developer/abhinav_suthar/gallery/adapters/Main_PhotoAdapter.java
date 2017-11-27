package com.developer.abhinav_suthar.gallery.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.activities.Photo1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Main_PhotoAdapter extends RecyclerView.Adapter<Main_PhotoAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    public Main_PhotoAdapter(Context context, ArrayList<HashMap<String, String>> albumList){
        this.context = context;
        this.albumList = albumList;

    }

    @Override
    public Main_PhotoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_0, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final Main_PhotoAdapter.MyViewHolder holder, int position) {
        final HashMap<String, String> photoAlbumDetail = albumList.get(position);

        Glide.with(context)
                .load(new File(photoAlbumDetail.get("key_path")))
                .into(holder.albumImage);

        holder.albumTitle.setText(photoAlbumDetail.get("key_album"));
        holder.albumCount.setText(photoAlbumDetail.get("key_countPhoto"));

        holder.albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Photo1.class);
                intent.putExtra("key_album", photoAlbumDetail.get("key_album"));
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView albumImage;
        private TextView albumTitle, albumCount;
        private MyViewHolder(View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            albumCount = itemView.findViewById(R.id.albumCount);
        }
    }
}
