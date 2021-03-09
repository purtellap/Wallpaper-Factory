package com.austinpurtell.wf;

import androidx.room.Room;
import android.content.Context;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.database.RawObject;
/*
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetFileDescriptor;
import android.opengl.GLSurfaceView;
import com.austinpurtell.wf.gl.GLBitmapRenderer;
import com.austinpurtell.wf.gl.GLBitmapRenderer30;
import com.austinpurtell.wf.gl.GLWallpaperService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
*/
import com.austinpurtell.wf.objects.ForegroundObject;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WPService extends WallpaperService {
    //our custom wallpaper
    public WPService() {
        super();
    }

    @Override
    public WPService.Engine onCreateEngine() {

        try {
            return new WPEngine();
        } catch (Exception e) {
            return new NyanEngine();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // ENGINE -- nested in WPService
    private class WPEngine extends WPService.Engine {
        private boolean visible = false;
        private final Handler mHandler = new Handler();

        private Background background;
        private ArrayList<ForegroundObject> objects;
        private SurfaceHolder holder;
        private Resources resources;

        private MediaPlayer mediaPlayer;

        WPEngine() {
            try{
                background = MainActivity.backgroundAdapterBackgrounds.get(0);
                objects = MainActivity.foregroundAdapterObjects;
            }
            catch (Exception e){
                WPService.updateMainArraysAsync updateMainArraysAsync = new WPService.updateMainArraysAsync(getApplicationContext(), this);
                updateMainArraysAsync.execute();
            }
            this.resources = getApplicationContext().getResources();

        }

        private final Runnable runnable = new Runnable()
        {
            @Override
            public void run() {
                if(!background.usesVideo()){
                    draw(holder);
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                if(background.usesVideo() && mediaPlayer != null){
                    mediaPlayer.start();
                }
                if(!background.usesVideo()){
                    draw(holder);
                }
            } else {
                mHandler.removeCallbacks(runnable);
                if(background.usesVideo() && mediaPlayer != null){
                    mediaPlayer.pause();
                }
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder){

            this.holder = holder;
            if(background.usesVideo()){
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setSurface(holder.getSurface());
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(background.getPathName());
                    mediaPlayer.setVolume(0, 0);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(!background.usesVideo()){
                draw(holder);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            visible = false;
            mHandler.removeCallbacks(runnable);

            if(background.usesVideo() && mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            visible = false;
            mHandler.removeCallbacks(runnable);
            if (mediaPlayer != null && background.usesVideo()){
                mediaPlayer.release();
            }
        }

        private void draw(SurfaceHolder holder) {

            Canvas c = null;
            try {
                //this is where you draw objects to canvas
                if(android.os.Build.VERSION.SDK_INT >= 26){
                    c = holder.lockHardwareCanvas();
                }
                else{
                    c = holder.lockCanvas();
                }

                if (c != null) {

                    /*ArrayList<Rect> boundingboxes = new ArrayList<>();
                    for (ForegroundObject object : objects){
                        if(object.isEnabled()) {
                            boundingboxes.add(object.getBoundingRect());
                        }
                    }
                    background.draw(c, resources, boundingboxes);*/

                    // actual code
                    if(background != null && !background.usesVideo()){
                        background.draw(c, resources);
                    }
                    if(objects!= null){
                        for (ForegroundObject object : objects){
                            if(object.isEnabled()) {
                                object.draw(c);
                            }
                        }
                    }

                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(runnable);
            if (visible) {
                mHandler.postDelayed(runnable, 0);
            }
        }


    }


    /*private class VideoBkgEngine extends Engine {

        private MediaPlayer mediaPlayer;
        private Background background;

        VideoBkgEngine() {
            try{
                background = MainActivity.backgroundAdapterBackgrounds.get(0);
                //objects = MainActivity.foregroundAdapterObjects;
            }
            catch (Exception e){
                //WPService.updateMainArraysAsync updateMainArraysAsync = new WPService.updateMainArraysAsync(getApplicationContext(), this);
                //updateMainArraysAsync.execute();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(holder.getSurface());
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(background.getPathName());
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                mediaPlayer.start();
            } else {
                mediaPlayer.pause();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mediaPlayer != null) mediaPlayer.release();
        }

        *//*private void sendAssetToInternalStorage() {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("wf_vids", Context.MODE_PRIVATE);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, imageFileName);
            if (!file.exists()) {
                Log.d("path", file.toString());
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }*//*
    }*/

    /*private class GlVideoEngine extends Engine {

        private MediaPlayer mediaPlayer;
        private Background background;
        private ArrayList<ForegroundObject> objects;
        private SurfaceHolder holder;
        private Resources resources;
        private GLWallpaperSurfaceView glSurfaceView;

        private class GLWallpaperSurfaceView extends GLSurfaceView {

            public GLWallpaperSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            void onDestroy() {
                super.onDetachedFromWindow();
            }
        }

        GlVideoEngine() {
            try{
                background = MainActivity.backgroundAdapterBackgrounds.get(0);
                objects = MainActivity.foregroundAdapterObjects;
            }
            catch (Exception e){
                //WPService.updateMainArraysAsync updateMainArraysAsync = new WPService.updateMainArraysAsync(getApplicationContext(), this);
                //updateMainArraysAsync.execute();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            *//*mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(holder.getSurface());
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(background.getPathName());
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }*//*

            createGLSurfaceView(getApplicationContext());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                //mediaPlayer.start();
                glSurfaceView.onResume();
            } else {
                //mediaPlayer.pause();
                glSurfaceView.onPause();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            *//*if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;*//*

            glSurfaceView.onDestroy();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mediaPlayer != null) mediaPlayer.release();
        }

        private void createGLSurfaceView(Context c) {
            if (glSurfaceView != null) {
                glSurfaceView.onDestroy();
                glSurfaceView = null;
            }
            glSurfaceView = new GLWallpaperSurfaceView(c);
            *//*final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                throw new RuntimeException("Cannot get ActivityManager");
            }
            final ConfigurationInfo configInfo = activityManager.getDeviceConfigurationInfo();*//*
            *//*if (configInfo.reqGlEsVersion >= 0x30000) {
                Log.d(TAG, "Support GLESv3");
                glSurfaceView.setEGLContextClientVersion(3);
                renderer = new GLES30WallpaperRenderer(context);
            } else if (configInfo.reqGlEsVersion >= 0x20000) {
                Log.d(TAG, "Fallback to GLESv2");
                glSurfaceView.setEGLContextClientVersion(2);
                renderer = new GLES20WallpaperRenderer(context);
            } else {
                Toast.makeText(context, "Needs GLESv2 or higher", Toast.LENGTH_LONG).show();
                throw new RuntimeException("Needs GLESv2 or higher");
            }*//*
            //glSurfaceView.setPreserveEGLContextOnPause(true);
            //glSurfaceView.setRenderer(renderer);
            // On demand render will lead to black screen.
            //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            glSurfaceView.setPreserveEGLContextOnPause(true);
            glSurfaceView.setEGLContextClientVersion(1);
            glSurfaceView.setRenderer(new GLBitmapRenderer(getResources(), objects));
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }

    }*/

    class NyanEngine extends Engine {
        private Movie mNyan;
        private int mNyanDuration;
        private final Runnable mNyanNyan;
        float mScaleX;
        float mScaleY;
        int mWhen;
        long mStart;

        final Handler mNyanHandler = new Handler();

        NyanEngine() {
            try{
                InputStream is = getResources().getAssets().open("default/crash/crash.gif");
                if (is != null) {
                    mNyan = Movie.decodeStream(is);
                    mNyanDuration = mNyan.duration();
                    is.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            mWhen = -1;
            mNyanNyan = new Runnable() {
                public void run() {
                    nyan();
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mNyanHandler.removeCallbacks(mNyanNyan);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                nyan();
            } else {
                mNyanHandler.removeCallbacks(mNyanNyan);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mScaleX = width / (1f * mNyan.width());
            mScaleY = height / (1f * mNyan.height());
            nyan();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                                     float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            nyan();
        }

        void nyan() {
            tick();
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    nyanNyan(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            mNyanHandler.removeCallbacks(mNyanNyan);
            if (isVisible()) {
                mNyanHandler.postDelayed(mNyanNyan, 1000L/25L);
            }
        }

        void tick() {
            if (mWhen == -1L) {
                mWhen = 0;
                mStart = SystemClock.uptimeMillis();
            } else {
                long mDiff = SystemClock.uptimeMillis() - mStart;
                mWhen = (int) (mDiff % mNyanDuration);
            }
        }

        void nyanNyan(Canvas canvas) {
            canvas.save();
            canvas.scale(1, 1);
            mNyan.setTime(mWhen);
            canvas.drawColor(0xff000000);
            mNyan.draw(canvas, (MainActivity.WIDTH/2f)-(mNyan.width()/2f), (MainActivity.HEIGHT/2f)-(mNyan.height()/2f));
            canvas.restore();
        }
    }

    static class updateMainArraysAsync extends AsyncTask<Void, Void, Void> {

        private Context context;
        private WPEngine engine;

        public updateMainArraysAsync(Context c, WPEngine engine){
            this.context = c;
            this.engine = engine;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = Room.databaseBuilder(context, AppDatabase.class, MainActivity.DB_NAME).build();

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();
            ArrayList<Background> backgrounds = (ArrayList<Background>) database.backgroundDao().getBackgrounds();

            ArrayList<ForegroundObject> foregroundObjects = new ArrayList<>();

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                ForegroundObject obj = new ForegroundObject(context, r.getId(), r.isEnabled(),
                        r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(), r.isChangeOnBounce(),
                        r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());
                foregroundObjects.add(obj);
            }
            MainActivity.foregroundAdapterObjects = foregroundObjects;

            if(backgrounds.size() > 0) {
                MainActivity.backgroundAdapterBackgrounds = backgrounds;
            }
            else{
                Background background = new Background(0, Color.argb(0xff, 0x11, 0x11, 0x11));
                database.backgroundDao().insertBackground(background);
                MainActivity.backgroundAdapterBackgrounds.add(background);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            this.engine.background = MainActivity.backgroundAdapterBackgrounds.get(0);
            this.engine.objects = MainActivity.foregroundAdapterObjects;

        }

    }

}
