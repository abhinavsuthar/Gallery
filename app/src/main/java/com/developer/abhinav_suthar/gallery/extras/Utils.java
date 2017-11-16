package com.developer.abhinav_suthar.gallery.extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;

import com.developer.abhinav_suthar.gallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    //--------------------------------------------------------------------------------------------//
    //MediaList stored for Photo1 to Photo2 and Video1 to Video2 to service
    private static ArrayList<HashMap<String, String>> mediaList = new ArrayList<>();
    public static void setMediaList(ArrayList<HashMap<String, String>> mediaListt){mediaList = mediaListt;}
    public static ArrayList<HashMap<String, String>> getMediaList(){return mediaList; }

    //--------------------------------------------------------------------------------------------//
    //Send data to copy service to get which item it has to copy
    private static Boolean[] selectedMediaItems;
    public static void setSelectedMediaItems(Boolean[] b){
        selectedMediaItems = null;
        //selectedMediaItems = b.clone();
        selectedMediaItems = new Boolean[b.length];
        System.arraycopy(b, 0, selectedMediaItems, 0, b.length);
    }
    public static Boolean[] getSelectedMediaItems(){
        return selectedMediaItems;
    }

    //--------------------------------------------------------------------------------------------//
    //Used to store bucket album name and path //Photos
    private static ArrayList<String> AlbumName = new ArrayList<>();
    private static ArrayList<String> AlbumPath = new ArrayList<>();
    private static Uri tempImagePath;
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
    public static ArrayList<String> getImageAlbumNames(){ return AlbumName; }
    public static ArrayList<String> getImageAlbumPath(){
        return AlbumPath;
    }
    public static void setTempImagePath(Uri path){
        tempImagePath = path;
    }
    public static Uri getTempImagePath(){
        return tempImagePath;
    }

    //--------------------------------------------------------------------------------------------//
    //Used to store bucket album name and path //Videos
    private static ArrayList<String> AlbumNameV = new ArrayList<>();
    private static ArrayList<String> AlbumPathV = new ArrayList<>();
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

    //--------------------------------------------------------------------------------------------//
    //It will modify the context sort menu on the basis of stored value in shared preference
    public static void sortCase(Menu menu, Context context, String albumName){
        SharedPreferences sp = context.getSharedPreferences(albumName, MODE_PRIVATE);
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

    //--------------------------------------------------------------------------------------------//
    //If new media file is added or deleted then this function will called
    private static boolean isMediaListIsChanged;
    public static void setIsMediaListIsChanged(boolean b){isMediaListIsChanged = b;}
    public static boolean getIsMediaListIsChanged(){return isMediaListIsChanged;}


}
