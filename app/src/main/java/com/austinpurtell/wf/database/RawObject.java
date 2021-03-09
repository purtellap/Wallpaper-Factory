package com.austinpurtell.wf.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.graphics.Color;

import com.austinpurtell.wf.objects.Packs;

import java.util.Random;

@Entity
public class RawObject {

    @PrimaryKey
    private int id;

    private boolean isEnabled = true;
    private String imageName = Packs.getDefaultImagePath(); // Default image
    private boolean usesLibraryImage = false;
    private boolean useColor = true;
    private int color = Color.argb(255, new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
    private boolean changeOnBounce = true;
    private int size = 50;
    private int speed = 8;
    private int angle = (new Random().nextInt(50)) + 20 + (90 * new Random().nextInt(4)); // avoid clean angles
    private boolean usesGravity = false;
    private boolean usesShadow = true;
    private boolean flipXonBounce = false;
    private boolean flipYonBounce = false;

    public RawObject(int id){
        this.id = id;
    }

    public RawObject(int id, boolean is, String name, boolean uI, boolean uC, int col, boolean cob, int si, int sp, int an, boolean uG, boolean uS, boolean flipX, boolean flipY) {
        this.id = id;
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
    }

    public int getId() {
        return id;
    }

    public void setId( int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isUseColor() {
        return useColor;
    }

    public String getImageName() {
        return imageName;
    }

    public int getColor() { return color; }

    public boolean isChangeOnBounce() { return changeOnBounce; }

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

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setColor(int color) { this.color = color; }

    public void setUseColor(boolean useColor) {
        this.useColor = useColor;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setChangeOnBounce(boolean changeOnBounce) { this.changeOnBounce = changeOnBounce; }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public boolean isUsesLibraryImage() { return usesLibraryImage; }

    public void setUsesLibraryImage(boolean usesLibraryImage) { this.usesLibraryImage = usesLibraryImage; }

    public boolean usesGravity() { return usesGravity; }

    public void setUsesGravity(boolean usesGravity) { this.usesGravity = usesGravity; }

    public boolean usesShadow() { return usesShadow; }

    public void setUsesShadow(boolean usesShadow) { this.usesShadow = usesShadow; }

    public boolean isFlipXonBounce() { return flipXonBounce; }

    public void setFlipXonBounce(boolean flipXonBounce) { this.flipXonBounce = flipXonBounce; }

    public boolean isFlipYonBounce() { return flipYonBounce; }

    public void setFlipYonBounce(boolean flipYonBounce) { this.flipYonBounce = flipYonBounce; }
}
