package com.austinpurtell.wf.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.austinpurtell.wf.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Packs {

    // default images that come with the app
    public static String folder_icons = "/icons/";
    public static String folder_gifs = "/gifs/";
    public static String folder_imgBkgs = "/imgBkg/";
    public static String folder_mp4s = "/mp4s/";
    public static String folder_promo = "/promo/";

    private static String defaultIconName = "_dvd_p.png";

    public enum Pack {
        /*DEFAULT ("default", true, "Wallpaper Factory Pack"),
        PLANET ("planet", false, "Solar System Pack"),
        FRUIT ("fruit", false, "Fruit Pack");*/

        DEFAULT ("default", true, "Wallpaper Factory Pack");

        // essentials
        private final String assetFolderName;
        private boolean isUnlocked;
        private ArrayList<PackObject> packObjects = new ArrayList<>();

        // promo
        private String promoTitle;
        private Bitmap promoImage;

        Pack(String folderName, boolean unlocked, String title) {
            this.assetFolderName = folderName;
            this.isUnlocked = unlocked;
            this.promoTitle = title;
        }

        public boolean isUnlocked(){return isUnlocked;}
        public String getPromoTitle(){return promoTitle;}
        public Bitmap getPromoImage(){return promoImage;}
        public String getAssetFolderName(){return assetFolderName;}

        public ArrayList<PackObject> getPackObjs(){return packObjects;}

        private void generatePackObjects(Context c){
            packObjects = new ArrayList<>();
            try{
                String[] iconsList =  c.getAssets().list(assetFolderName + folder_icons); // gets icons only
                for (String s : iconsList){
                    packObjects.add(new PackObject(assetFolderName + folder_icons + s, isUnlocked));
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        private void setUnlockedFromPrefs(Context c){
            isUnlocked = c.getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).getBoolean(assetFolderName, isUnlocked);
        }

        private void generatePromo(Context c){
            try{
                // first image will be promo image (if I add more media they will have to be lower alphabetically)
                String[] promoList =  c.getAssets().list(assetFolderName + folder_promo);
                Bitmap unscaled = Packs.getBitmapFromAsset(c, assetFolderName + folder_promo + promoList[0]);
                this.promoImage = unscaled;
            }
            catch (Exception e){
                Log.d("Missing some images?", e.getMessage());
            }
        }

        private void unlock(Context c){
            isUnlocked = true;
            SharedPreferences.Editor editor = c.getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).edit();
            editor.putBoolean(assetFolderName, isUnlocked).apply();
        }

    }

    public static String getDefaultImagePath(){
        return Pack.DEFAULT.assetFolderName + folder_icons + defaultIconName;
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream i;
        Bitmap bitmap = null;
        try {
            i = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    // run from main to initialize
    public static ArrayList<PackObject> getAllObjects(Context context) {

         Pack[] packs = Pack.values();
         ArrayList<PackObject> allPackObjs = new ArrayList<>();

         for (Pack pack : packs){
             pack.setUnlockedFromPrefs(context);
             pack.generatePackObjects(context);
             for (PackObject packObject : pack.getPackObjs()){
                 packObject.makeImage(context);
                 allPackObjs.add(packObject);
             }
         }

        return allPackObjs;
    }

    public static ArrayList<Pack> getPacksForPromo(Context context){

        Pack[] packs = Pack.values();
        ArrayList<Pack> promoPacks = new ArrayList<>();

        for (Pack pack : packs){
            pack.setUnlockedFromPrefs(context);

            pack.generatePromo(context);
            promoPacks.add(pack);

        }

        return promoPacks;

    }
}
