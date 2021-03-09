package com.austinpurtell.wf.extras;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class GifImageView extends View {

    private Movie gif;
    private int mWidth, mHeight;
    private long mStart;

    public GifImageView(Context context) {
        super(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long now = SystemClock.uptimeMillis();

        if (mStart == 0) {
            mStart = now;
        }

        if (gif != null) {

            int duration = gif.duration();
            if (duration == 0) {
                duration = 1000;
            }

            int relTime = (int) ((now - mStart) % duration);

            gif.setTime(relTime);

            canvas.scale(this.getWidth()/(float)mWidth, this.getHeight()/(float)mHeight);

            gif.draw(canvas, 0, 0);
            invalidate();
        }
    }

    public void setGVfromLibGif(Movie g, int w, int h, long s){
        this.gif = g;
        this.mWidth = w;
        this.mHeight = h;
        this.mStart = s;
        setFocusable(true);
        requestLayout();
    }
}
