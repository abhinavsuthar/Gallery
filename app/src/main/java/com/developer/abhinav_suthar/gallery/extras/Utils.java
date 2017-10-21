package com.developer.abhinav_suthar.gallery.extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Menu;

import com.developer.abhinav_suthar.gallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Abhinav_Suthar on 26-08-2017.
 */

public class Utils {

    //Photos
    private static ArrayList<String> AlbumName = new ArrayList<>();
    private static ArrayList<String> AlbumPath = new ArrayList<>();
    private static Uri tempImagePath;

    //Videos
    private static ArrayList<String> AlbumNameV = new ArrayList<>();
    private static ArrayList<String> AlbumPathV = new ArrayList<>();

    //Photos
    public static void ImageAlbumDetails(ArrayList<HashMap<String, String>> AlbumList){
        AlbumName.clear();
        AlbumPath.clear();
        for (int i=0;i<AlbumList.size();i++){
            AlbumName.add(AlbumList.get(i).get("key_album"));
            //Directory Path
            File file = new File(AlbumList.get(i).get("key_path"));
            file = new File(file.getAbsolutePath());
            AlbumPath.add(file.getParent());
        }
        AlbumName.add("Make New Folder ! ! !");
    }

    public static ArrayList<String> getImageAlbumNames(){
        return AlbumName;
    }

    public static ArrayList<String> getImageAlbumPath(){
        return AlbumPath;
    }

    public static void sortCase(Menu menu, Context context, String albumName){
        SharedPreferences sp = context.getSharedPreferences(albumName, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
        int index = sp.getInt("key_sort", 56);

        menu.findItem(R.id.photo_1_action_title).setChecked(false);
        menu.findItem(R.id.photo_1_action_size).setChecked(false);
        menu.findItem(R.id.photo_1_action_date).setChecked(false);
        menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(false);
        menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(false);

        switch (index){
            case 51:
                menu.findItem(R.id.photo_1_action_title).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 52:
                menu.findItem(R.id.photo_1_action_title).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
            case 53:
                menu.findItem(R.id.photo_1_action_size).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 54:
                menu.findItem(R.id.photo_1_action_size).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
            case 55:
                menu.findItem(R.id.photo_1_action_date).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 56:
                menu.findItem(R.id.photo_1_action_date).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
        }
    }

    public static void setTempImagePath(Uri path){
        tempImagePath = path;
    }

    public static Uri getTempImagePath(){
        return tempImagePath;
    }

    //Videos
    public static void VideoAlbumDetails(ArrayList<HashMap<String, String>> AlbumList){
        AlbumNameV.clear();
        AlbumPathV.clear();
        for (int i=0;i<AlbumList.size();i++){
            AlbumNameV.add(AlbumList.get(i).get("key_album"));
            //Directory Path
            File file = new File(AlbumList.get(i).get("key_path"));
            file = new File(file.getAbsolutePath());
            AlbumPathV.add(file.getParent());
        }
        AlbumNameV.add("Make New Folder ! ! !");
    }

    public static ArrayList<String> getVideoAlbumNames(){
        return AlbumNameV;
    }

    public static ArrayList<String> getVideoAlbumPath(){
        return AlbumPathV;
    }
}
