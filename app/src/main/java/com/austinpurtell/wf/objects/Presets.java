package com.austinpurtell.wf.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.database.RawObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.austinpurtell.wf.objects.Packs.folder_icons;

public class Presets {

    // default images that come with the app
    private static String folder_presets = "/presets/";

    public enum Preset {
        CLASSIC (Packs.Pack.DEFAULT, "The Classic", "dvdpreset.mp4"),
        BALLS (Packs.Pack.DEFAULT, "Bouncy Balls", "ballspreset.mp4");

        Packs.Pack packType;
        String presetTitle;
        String presetVidPath;

        Preset(Packs.Pack pack, String title, String presetVidName) {

            this.packType = pack;
            this.presetTitle = title;
            this.presetVidPath = pack.getAssetFolderName() + folder_presets + presetVidName;
        }

        public String getPresetTitle(){ return this.presetTitle; }
        public String getPresetVidPath(){ return this.presetVidPath; }

        /*public ArrayList<ForegroundObject> getPresetObjects(){

            ArrayList<ForegroundObject> objects = new ArrayList<>();

            if(this.equals(CLASSIC)){
                ForegroundObject obj = new ForegroundObject();
            }


            return objects;
        }*/

        public ArrayList<RawObject> getPresetForeground(int lastID, SharedPreferences.Editor editor){
            ArrayList<RawObject> objects = new ArrayList<>();
            if(this.equals(CLASSIC)){
                editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID+1);
                editor.apply();
                objects.add(new RawObject(lastID+1));
                return objects;
            }
            else if(this.equals(BALLS)){
                int num = 4;
                editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID + num);
                editor.apply();

                String name = packType.getAssetFolderName() + folder_icons + "circle.png";
                List<Integer> colors = Arrays.asList(
                        Color.argb(0xff, 0x00, 0xd1, 0xff),
                        Color.argb(0xff, 0x50, 0x16, 0xff),
                        Color.argb(0xff, 0x28, 0xdd, 0x00),
                        Color.argb(0xff, 0xff, 0x1d, 0x59));

                for(int i = 1; i <= num; i++){
                    int angle = new Random().nextInt(360);
                    objects.add(new RawObject(lastID + i, true, name, false, true, colors.get(i-1),
                            false, 15, 16, angle, true, true, false, false));
                }
                return objects;
            }
            else{
                Log.d("Error","No foreground preset");
                return null;
            }
        }

        public Background getPresetBackground(){
            if(this.equals(CLASSIC)){
                return new Background(0, Color.argb(0xff, 0x11, 0x11, 0x11));
            }
            else if(this.equals(BALLS)){
                return new Background(0, Color.argb(0xff, 0x00, 0x00, 0x00));
            }
            else{
                Log.d("Error","No background preset");
                return null;
            }
        }

    }

    public static ArrayList<Preset> getPresets(Context context){

        Preset[] presets = Preset.values();
        ArrayList<Preset> allpresets = new ArrayList<>();

        for (Preset p : presets){
            allpresets.add(p);
        }

        return allpresets;

    }


}
