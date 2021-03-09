package com.austinpurtell.wf.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.austinpurtell.wf.MainActivity;

public class PackObject {

    private String imageName;
    private boolean isUnlocked;
    private Bitmap image;

    public PackObject(String imageName, boolean iU) {
        this.imageName = imageName;
        this.isUnlocked = iU;
    }

    public void makeImage(Context c) {
        Bitmap unscaled = Packs.getBitmapFromAsset(c, this.imageName);
        float ar = unscaled.getHeight()/(float)unscaled.getWidth();
        float width = MainActivity.WIDTH * .3f ;
        this.image = Bitmap.createScaledBitmap(unscaled, (int)width, (int)(width*ar), false);
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
