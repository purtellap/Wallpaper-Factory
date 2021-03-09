package com.austinpurtell.wf.extras;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;

import com.austinpurtell.wf.MainActivity;

public class GlowDrawable extends ShapeDrawable {

    private float mCenterX = 0.0f;
    private float mCenterY = 0.0f;
    private float mOffsetX = 40.0f;
    private float mOffsetY = 80.0f;
    private float mRadius = 0.0f;
    private float mSpeedX = 1.0f;
    private float mSpeedY = 2.0f;

    private int screenWidth = MainActivity.WIDTH;
    private int screenHeight = MainActivity.HEIGHT;

    // alpha channel causes lag
    private int mColorFG = 0xffffff00; // yellow
    private int mColorBG = 0xffff6633; // orange

    Paint paint = new Paint();

    public GlowDrawable() {
        setBounds();
        paint.setShader(new RadialGradient(mCenterX, mCenterY, mRadius, mColorFG, mColorBG, Shader.TileMode.CLAMP));
    }

    public void setBounds() {
        if (mRadius == 0.0f) {
            mCenterX = (screenWidth)/2.0f;
            mCenterY = (screenHeight)/2.0f;
            mRadius = mCenterX + mCenterY;
        }
    }

    public void update() {
        mCenterX += mSpeedX;
        mCenterY += mSpeedY;

        if (mCenterX < 0  || mCenterX > screenWidth - mOffsetX) {
            mSpeedX *= -1.0f;
        }

        if (mCenterY < 0 ||
                mCenterY > screenHeight - mOffsetY) {
            mSpeedY *= -1.0f;
        }
        paint.setShader(new RadialGradient(mCenterX, mCenterY, mRadius, mColorFG, mColorBG, Shader.TileMode.CLAMP));
    }

    public void draw(Canvas c) {
        update();
        c.drawRect(0, 0, screenWidth, screenHeight, paint);
    }
}
