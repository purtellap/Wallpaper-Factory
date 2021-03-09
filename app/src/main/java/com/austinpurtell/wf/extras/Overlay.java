package com.austinpurtell.wf.extras;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.austinpurtell.wf.R;

import static com.austinpurtell.wf.adapters.DefaultAdapter.drawableToBitmap;

public class Overlay extends AppCompatImageView {

    private final Paint paint = new Paint();
    private Bitmap bitmap;

    public Overlay(Context context) {
        super(context);

        bitmap = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_lock_24px));

        //paint.setColorFilter(new PorterDuffColorFilter(0x66666677, PorterDuff.Mode.SRC_OUT));
        paint.setColor(0x66ccccff);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
    }

    public Overlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Overlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        /**//*

        holder.cardView.draw(canvas);*/
        //canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawPaint(paint);

    }
}
