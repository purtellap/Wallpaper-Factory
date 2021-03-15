package com.austinpurtell.wf.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.Log;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;

import java.io.InputStream;

@Entity
public class Background {

    @PrimaryKey
    private int id;

    private int color = Color.argb(0xFF, 0x11, 0x11, 0x11);
    private boolean usesImage = false;
    private boolean usesGif = false;
    private boolean usesVideo = false;
    private String pathName;

    @Ignore private Bitmap image;
    @Ignore private Movie gif;
    @Ignore int time;
    @Ignore long start;
    @Ignore private int duration;
    @Ignore private Paint paint = new Paint();
    @Ignore private float left;
    @Ignore private float top;

    public Background(int id, int color){
        this.id = id;
        this.color = color;
    }

    public Background(int id, String in, Context c) {
        this.id = id;
        this.usesImage = true;
        this.pathName = in;

        setImage(c.getResources());
    }

    // gif background only!!
    public Background(int id, String in, int i) {
        this.id = id;
        this.usesGif = true;
        this.pathName = in;
    }

    // video background only!!
    public Background(int id, String in) {
        this.id = id;
        this.usesVideo = true;
        this.pathName = in;
    }

    public void draw(Canvas canvas, Resources r) {
        //update();
        if(!usesImage && !usesGif && !usesVideo) {

            // dragging effect
            //this.color = Color.argb(0xcc, Color.red(this.color), Color.green(this.color), Color.blue(this.color));
            canvas.drawColor(this.color);
        }
        else if (usesImage){
            /*WallpaperManager myWallpaperManager = WallpaperManager.getInstance(c);
            this.image = drawableToBitmap(myWallpaperManager.getDrawable());*/

            if(this.image == null){
                setImage(r);
            }

            //glowDrawable.draw(canvas);
            // draw black just to cover whole screen
            /*canvas.drawColor(Color.argb(0x01, 0xFF, 0xFF, 0xFF));
            // draw image
            if(this.firstFrame){
                canvas.drawColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
                //canvas.drawBitmap(this.image, this.left, this.top, paint);
                firstFrame = false;
            }*/
            /*for (Rect rect : rects){
                //canvas.drawRect(rect, new Paint());
                canvas.drawBitmap(this.image, rect, rect, paint);
            }*/
            //canvas.drawBitmap(this.image, this.left, this.top, paint);

            canvas.drawColor(Color.argb(0xFF, 0x00, 0x00, 0x00));
            canvas.drawBitmap(this.image, this.left, this.top, paint);
        }
        else if(usesGif){
            if(this.gif == null){
                setGif(r);
            }
            tick();

            canvas.drawColor(Color.argb(0xFF, 0x00, 0x00, 0x00));
            drawGif(canvas, time);
        }
    }

    void drawGif(Canvas canvas, int time) {
        canvas.save();
        canvas.scale(MainActivity.WIDTH/(float)gif.width(), MainActivity.HEIGHT/(float)gif.height());
        gif.setTime(time);
        gif.draw(canvas, 0, 0);
        canvas.restore();
    }

    void tick() {
        if (time == -1L) {
            time = 0;
            start = SystemClock.uptimeMillis();
        } else {
            long mDiff = SystemClock.uptimeMillis() - start;
            time = (int) (mDiff % duration);
        }
        //Log.d("time", time+"");
    }

    public void setGif(Resources r){
        try {
            gif = Movie.decodeFile(pathName);
            if(gif == null){
                InputStream is = r.getAssets().open("default/crash/crash.gif");
                gif = Movie.decodeStream(is);
            }

            duration = gif.duration();
            //is.close();
        } catch (Exception e){
            Log.d("Error", e.getMessage());
        }

        time = -1;
    }

    private void setImage(Resources r){
        String filepath = this.pathName;
        Bitmap bit = null;
        try{
            bit = BitmapFactory.decodeFile(filepath);
        }
        catch (Exception e){
            Log.d("Error", e.getMessage());
        }
        if (bit != null){
            float ar = bit.getHeight()/(float)bit.getWidth();
            float width = MainActivity.WIDTH;
            this.image = Bitmap.createScaledBitmap(bit, (int)width, (int)(width*ar), false);
        }
        else{
            bit = BitmapFactory.decodeResource(r, R.raw.deleted);
            this.image = Bitmap.createScaledBitmap(bit, bit.getWidth()/2, bit.getHeight()/2, false);
        }

        this.left = (MainActivity.WIDTH - this.image.getWidth())/2f;
        this.top = (MainActivity.HEIGHT - this.image.getHeight())/2f;
        this.image.prepareToDraw();
    }

    public Bitmap makePreviewImage(Context c){
        String filepath = this.pathName;
        Bitmap bit = null;
        try{
            bit = BitmapFactory.decodeFile(filepath);
        }
        catch (Exception e){
            Log.d("Error", e.getMessage());
        }
        if (bit != null){
            float ar = bit.getHeight()/(float)bit.getWidth();
            float width = MainActivity.WIDTH / 2f;
            bit = Bitmap.createScaledBitmap(bit, (int)width, (int)(width*ar), false);
        }
        else{
            bit = BitmapFactory.decodeResource(c.getResources(), R.raw.deleted);
        }
        return bit;
    }

   /*   private static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
*/
    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public boolean usesImage() { return usesImage; }

    public boolean usesVideo() {
        return usesVideo;
    }

    public String getPathName() {
        return pathName;
    }

    public Bitmap getImage() {
        return image;
    }

    public Movie getGif() { return gif; }

    // DAO only
    public void setId(int id) { this.id = id; }

    public void setColor(int color) { this.color = color; }

    public void setUsesImage(boolean usesImage) { this.usesImage = usesImage; }

    public void setUsesVideo(boolean usesVideo) { this.usesVideo = usesVideo; }

    public void setPathName(String pathName) { this.pathName = pathName; }

    public boolean usesGif() { return usesGif; }

    public void setUsesGif(boolean usesGif) { this.usesGif = usesGif; }
}
