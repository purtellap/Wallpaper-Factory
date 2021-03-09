package com.austinpurtell.wf.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;

@Entity
public class LibraryImage {

    @PrimaryKey
    private int id;

    private String imageName;
    private boolean isCircle = false;

    @Ignore
    private Bitmap image;

    public LibraryImage(int id){
        this.id = id;
    }

    public LibraryImage(int id, String name) {
        this.id = id;
        this.imageName = name;
    }

    public int getId() {
        return id;
    }

    public void setId( int id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setImage(Context c){
        String filepath = this.imageName;
        Bitmap bit = null;
        try{
            bit = BitmapFactory.decodeFile(filepath);
        }
        catch (Exception e){
            Log.d("Error", e.getMessage());
        }
        if (bit != null){
            float ar = bit.getHeight()/(float)bit.getWidth();
            float width = MainActivity.WIDTH / 3f;
            this.image = Bitmap.createScaledBitmap(bit, (int)width, (int)(width*ar), false);
        }
        else{
            this.image = BitmapFactory.decodeResource(c.getResources(), R.raw.deleted);
        }
        /**/
    }

    public Bitmap getImage() {
        return image;
    }

    public boolean isCircle() { return isCircle; }

    public void setCircle(boolean circle) { isCircle = circle; }
}
