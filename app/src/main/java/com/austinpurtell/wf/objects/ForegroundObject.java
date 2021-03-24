package com.austinpurtell.wf.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;

import java.util.ArrayList;
import java.util.Random;

public class ForegroundObject {

    // database params
    private int ID;
    private boolean isEnabled;
    private boolean useColor;
    private boolean usesLibraryImage;
    private int color;
    private boolean changeOnBounce;
    private String imageName;
    private int size;
    private int speed;
    private int angle;
    private boolean usesGravity;
    private boolean usesShadow;
    private boolean flipXonBounce;
    private boolean flipYonBounce;

    // non database
    private Bitmap image;

    private Bitmap shadowBitmap;
    private ArrayList<Bitmap> flippedBitmaps = new ArrayList<>();
    private ArrayList<Bitmap> flippedShadowBitmaps = new ArrayList<>();
    private boolean flippedX = false;
    private boolean flippedY = false;
    private int shape;
    private Paint paint = new Paint();
    private Paint shadowpaint = new Paint();
    private int shadowoffset = 8;
    private int xPos, yPos;
    private Rect boundingRect;

    private float gravity = 1f; // only value that works rn
    private float xVelocity;
    private float yVelocity;
    private int screenWidth = MainActivity.WIDTH;
    private int screenHeight = MainActivity.HEIGHT;

    // used for recording a preview for presets
    private float yOffset = 0f;
    //private float yOffset = 630f;

    public ForegroundObject(Context c, int id, boolean is, String name, boolean uI, boolean uC, int col, boolean cob, int si, int sp, int an, boolean uG, boolean uS, boolean flipX, boolean flipY) {
        this.ID = id;
        this.isEnabled = is;
        this.imageName = name;
        this.usesLibraryImage = uI;
        this.useColor = uC;
        this.color = col;
        this.changeOnBounce = cob;
        this.size = si;
        this.speed = sp;
        this.angle = an;
        this.usesGravity = uG;
        this.usesShadow = uS;
        this.flipXonBounce = flipX;
        this.flipYonBounce = flipY;

        // sets image
        if(!usesLibraryImage){
            setDefaultImage(c);
        }
        else{
            setLibraryImage(c);
        }

        // set position
        this.xPos = (screenWidth/2) - (image.getWidth()/2); // middle
        this.yPos = (screenHeight/2) - (image.getHeight()/2);

        // set speed
        this.xVelocity = speed * (float) Math.cos(Math.toRadians(angle));
        this.yVelocity = speed * (float) Math.sin(Math.toRadians(-angle));

        // set color
        if(useColor) {
            ColorFilter filter = new PorterDuffColorFilter(this.color, PorterDuff.Mode.SRC_IN);
            this.paint.setColorFilter(filter);
        }
        this.shadowpaint.setColorFilter(new PorterDuffColorFilter(0x55000000, PorterDuff.Mode.SRC_IN));
    }

    public void draw(Canvas canvas) {

        if(usesGravity){
            updateGravity();
        }
        else {
            update();
        }

        if(usesShadow){
            //makeShadowBitmap(); // for flipping we put this here for now
            canvas.drawBitmap(shadowBitmap, xPos + shadowoffset, yPos + shadowoffset, shadowpaint);
        }
        canvas.drawBitmap(image, xPos, yPos, paint);
    }

    public void updateGL(){

        if(usesGravity){
            updateGravity();
        }
        else {
            update();
        }

    }

    private void update() {

        if(xVelocity > 0f){
            xVelocity = Math.max(xVelocity, 1f);
        }
        else if (xVelocity < 0f){
            xVelocity = Math.min(xVelocity, -1f);
        }
        if(yVelocity > 0f){
            yVelocity = Math.max(yVelocity, 1f);
        }
        else if (yVelocity < 0f){
            yVelocity = Math.min(yVelocity, -1f);
        }

        xPos += xVelocity;
        yPos += yVelocity;

        if ((xPos > (float)(screenWidth - image.getWidth())) || (xPos < 0f)) {
            xVelocity = xVelocity * -1f;
            if(changeOnBounce){
                changeColor();
            }
            if(flipXonBounce){
                flippedX = !flippedX;
                updateBitmapFlip();
            }
        }
        if ((yPos > (float)(screenHeight - image.getHeight() - yOffset)) || (yPos < 0f + yOffset)) {
            yVelocity = yVelocity * -1f;
            if(changeOnBounce){
                changeColor();
            }
            if(flipYonBounce){
                flippedY = !flippedY;
                updateBitmapFlip();
            }
        }
    }

    private void updateGravity() {

        if(xVelocity > 0f){
            xVelocity = Math.max(xVelocity, 1f);
        }
        else if (xVelocity < 0f){
            xVelocity = Math.min(xVelocity, -1f);
        }

        //float gravity = 1f;
        yVelocity += gravity/2f;

        xPos += xVelocity;
        yPos += yVelocity;

        if ((xPos > (float)(screenWidth - image.getWidth())) || (xPos < 0f)) {
            xVelocity = xVelocity * -1f;
            if(changeOnBounce){
                changeColor();
            }
            if(flipXonBounce){
                flippedX = !flippedX;
                updateBitmapFlip();
            }
        }
        if ((yPos > (float)(screenHeight - image.getHeight() - yOffset)) || (yPos < 0f)) {

            /*if((yPos > (float)(screenHeight - image.getHeight()))){
                yPos = (screenHeight - image.getHeight());
            }
            else if(yPos < 0f){
                yPos = 0;
            }*/
            yVelocity = yVelocity * -1f;
            if(changeOnBounce){
                changeColor();
            }
            if(flipYonBounce){
                flippedY = !flippedY;
                updateBitmapFlip();
            }
        }
        yVelocity += gravity/2f;


        //Log.d("yVelocity", yVelocity + "");
        //yVelocity = Math.min(yVelocity, );

    }

    private void changeColor() {
        if (useColor) {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            paint.setColorFilter(filter);
        }
    }

    public void setDefaultImage(Context c){
        Bitmap unscaled = Packs.getBitmapFromAsset(c, this.imageName);
        float ar = unscaled.getHeight()/(float)unscaled.getWidth();
        float width = this.screenWidth * .7f * (size / 100f);
        image = Bitmap.createScaledBitmap(unscaled, (int)width, (int)(width*ar), false);
        generateFlippedBitmaps();
    }

    public void setLibraryImage(Context c){
        String filepath = this.imageName;
        Bitmap bit = null;
        try{
            bit = BitmapFactory.decodeFile(filepath);
        }
        catch (Exception e){
            //Log.d("Error", e.getMessage());
        }
        if (bit != null){
            float ar = bit.getHeight()/(float)bit.getWidth();
            if(bit.getHeight() > bit.getWidth()){
                // ar > (MainActivity.HEIGHT/MainActivity.WIDTH)
                // if(width > bit.getWidth() && bit.getHeight() > bit.getWidth()){
                float height = this.screenWidth * .7f * (size / 100f);
                this.image = Bitmap.createScaledBitmap(bit, (int)(height/ar), (int)height, false);
            }
            else{
                float width = this.screenWidth * .7f * (size / 100f);
                this.image = Bitmap.createScaledBitmap(bit, (int)width, (int)(width*ar), false);
            }
        }
        else{
            bit = BitmapFactory.decodeResource(c.getResources(), R.raw.deleted);
            float ar = bit.getHeight()/(float)bit.getWidth();
            float width = this.screenWidth * .7f * (size / 100f);
            this.image = Bitmap.createScaledBitmap(bit, (int)width, (int)(width*ar), false);
        }
        generateFlippedBitmaps();
    }

    private Bitmap generateShadowBitmap(){
        Bitmap b = Bitmap.createBitmap(this.image);
        if(Build.VERSION.SDK_INT >= 26){
            b.setConfig(Bitmap.Config.HARDWARE);
        }
        else{
            b.setConfig(Bitmap.Config.ARGB_8888);
        }
        return b;
    }

    private void setBoundingRect(){
        this.boundingRect = new Rect(xPos, yPos, xPos + image.getWidth() + shadowoffset, yPos + image.getHeight() + shadowoffset);
    }

    public Rect getBoundingRect(){
        setBoundingRect();
        return boundingRect;
    }

    private void updateBitmapFlip(){
        if (!flippedX && !flippedY){
            this.image = flippedBitmaps.get(0);
            this.shadowBitmap = flippedShadowBitmaps.get(0);
        }
        else if(flippedX && !flippedY){
            this.image = flippedBitmaps.get(1);
            this.shadowBitmap = flippedShadowBitmaps.get(1);
        }
        else if(!flippedX && flippedY){
            this.image = flippedBitmaps.get(2);
            this.shadowBitmap = flippedShadowBitmaps.get(2);
        }
        else if (flippedX && flippedY){
            this.image = flippedBitmaps.get(3);
            this.shadowBitmap = flippedShadowBitmaps.get(3);
        }
    }

    private void generateFlippedBitmaps(){
        // normal, flip x, flip y, flip xy
        this.flippedBitmaps.add(this.image);
        this.flippedBitmaps.add(flipBitmapHorizontally(this.image));
        this.flippedBitmaps.add(flipBitmapVertically(this.image));
        this.flippedBitmaps.add(flipBitmapVertically(flipBitmapHorizontally(this.image)));

        this.shadowBitmap = generateShadowBitmap();
        this.flippedShadowBitmaps.add(shadowBitmap);
        this.flippedShadowBitmaps.add(flipBitmapHorizontally(shadowBitmap));
        this.flippedShadowBitmaps.add(flipBitmapVertically(shadowBitmap));
        this.flippedShadowBitmaps.add(flipBitmapVertically(flipBitmapHorizontally(shadowBitmap)));
    }

    private Bitmap flipBitmapHorizontally(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    private Bitmap flipBitmapVertically(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    public Bitmap getImage() {
        return image;
    }

    public int getShape() {
        return shape;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean usesColor() {
        return useColor;
    }

    public String getImageName() {
        return imageName;
    }

    public int getColor() {
        return color;
    }

    public boolean changesOnBounce() {
        return changeOnBounce;
    }

    public int getSize() {
        return size;
    }

    public int getSpeed() {
        return speed;
    }

    public int getAngle() {
        return angle;
    }

    public void toggleEnabled() {
        isEnabled = !isEnabled;
    }

    public void setUseColor(boolean useColor) {
        this.useColor = useColor;
    }

    public void setChangeOnBounce(boolean changeOnBounce) {
        this.changeOnBounce = changeOnBounce;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setEnabled(boolean set) {
        isEnabled = set;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void setImageName(String imageName) { this.imageName = imageName; }

    public boolean isUsesLibraryImage() { return usesLibraryImage; }

    public void setUsesLibraryImage(boolean usesLibraryImage) { this.usesLibraryImage = usesLibraryImage; }

    public boolean usesGravity() { return usesGravity; }

    public void setUsesGravity(boolean usesGravity) { this.usesGravity = usesGravity; }

    public boolean usesShadow() { return usesShadow; }

    public void setUsesShadow(boolean usesShadow) { this.usesShadow = usesShadow;}

    public boolean flipsXonBounce() { return flipXonBounce; }

    public void setFlipXonBounce(boolean flipXonBounce) { this.flipXonBounce = flipXonBounce; }

    public boolean flipsYonBounce() { return flipYonBounce; }

    public void setFlipYonBounce(boolean flipYonBounce) { this.flipYonBounce = flipYonBounce;}

    }