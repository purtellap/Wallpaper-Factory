package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.database.RawObject;
import com.austinpurtell.wf.objects.ForegroundObject;
import com.austinpurtell.wf.objects.Presets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    View view;
    RecyclerView recyclerView;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView layout;
        TextView title;
        ImageView promoImage;
        TextView price;
        ImageView lockedIcon;
        VideoView videoView;

        MyViewHolder(CardView cardView, final HomeAdapter adapter, Context context) {
            super(cardView);
            layout = cardView;

            title = layout.findViewById(R.id.presetTitle);
            videoView = layout.findViewById(R.id.presetVideo);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0f,0f);
                    mp.setLooping(true);
                    videoView.start();
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    if(mp.isPlaying()){
                        mp.stop();
                    }
                    mp.reset();
                    mp.release();
                    mp = null;
                }
            });

            MainActivity.videoViews.add(videoView);

        }

    }

    public HomeAdapter(View view, RecyclerView recyclerView) {
        this.view = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        this.recyclerView = recyclerView;

        HomeAdapter.getPresetsAsync getObjectsAsync = new HomeAdapter.getPresetsAsync(view, recyclerView, this);
        getObjectsAsync.execute();

    }

    @Override
    public @NonNull HomeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_presets, parent, false);
        return new MyViewHolder(cardView, this, view.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.title.setText(MainActivity.presets.get(holder.getAdapterPosition()).getPresetTitle());
        //view.getContext().getAssets().open("assets/default/mp4s/test.mp4");
        String[] paths = MainActivity.presets.get(holder.getAdapterPosition()).getPresetVidPath().split("/");
        holder.videoView.setVideoPath(view.getContext().getDir(MainActivity.FOLDER_PRESET_VIDS, Context.MODE_PRIVATE) + File.separator + paths[paths.length-1]);
        //Log.d("path",view.getContext().getDir(MainActivity.FOLDER_PRESET_VIDS, Context.MODE_PRIVATE) + File.separator + paths[paths.length-1]);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeAdapter.setPresetAsync setPresetAsync = new HomeAdapter.setPresetAsync(view, holder.getAdapterPosition());
                setPresetAsync.execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return MainActivity.presets.size();
    }

    public static void savePresetVidsToStorage(View view, String assetPath){
        try {
            AssetFileDescriptor videoAsset = view.getContext().getAssets().openFd(assetPath);
            FileInputStream in = videoAsset.createInputStream();

            File dir = new File(view.getContext().getDir(MainActivity.FOLDER_PRESET_VIDS, Context.MODE_PRIVATE) + File.separator);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String[] paths = assetPath.split("/");
            File newfile = new File(dir, paths[paths.length-1]);
            OutputStream out = new FileOutputStream(newfile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class getPresetsAsync extends AsyncTask<Void, Void, ArrayList<Presets.Preset>> {

        private View view;
        private RecyclerView recyclerView;
        private HomeAdapter adapter;

        getPresetsAsync(View v, RecyclerView recyclerView, HomeAdapter adapter){
            //this.holder = holder;
            this.view = v;
            this.recyclerView = recyclerView;
            this.adapter = adapter;
        }

        @Override
        protected ArrayList<Presets.Preset> doInBackground(Void... voids) {

            ArrayList<Presets.Preset> presets = Presets.getPresets(view.getContext());

            SharedPreferences sharedPref =  view.getContext().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
            boolean firstTimeHome = sharedPref.getBoolean("firstTimeHome", true);
            if(firstTimeHome){
                for (Presets.Preset p : presets){
                    savePresetVidsToStorage(view, p.getPresetVidPath());
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstTimeHome", false);
                editor.apply();
            }
            return presets;
        }

        @Override
        protected void onPostExecute(ArrayList<Presets.Preset> asyncObjs) {
            super.onPostExecute(asyncObjs);

            MainActivity.presets = asyncObjs;

            ProgressBar spinner = (ProgressBar) adapter.recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);

            for (int i = 0; i < MainActivity.presets.size(); i++){
                //Log.d("ID", adapter.objects.get(i).getID() + "");
                recyclerView.scrollToPosition(0);
            }

        }
    }

    private static class setPresetAsync extends AsyncTask<Void, Void, Void> {

        private View view;
        private int position;

        setPresetAsync(View v, int pos){
            this.view = v;
            this.position = pos;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // Foreground
            // get objects
            ArrayList<ForegroundObject> asyncObjects = new ArrayList<>();
            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) MainActivity.objectDB.objectDao().getAllObjects();
            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                ForegroundObject obj = new ForegroundObject(view.getContext(), r.getId(), r.isEnabled(),
                        r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(), r.isChangeOnBounce(),
                        r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());
                asyncObjects.add(obj);
            }
            MainActivity.foregroundAdapterObjects = asyncObjects;

            // disable current objs
            for (int i = 0; i < rawObjects.size(); i++){
                RawObject copy = rawObjects.get(i);
                MainActivity.objectDB.objectDao().removeObject(rawObjects.get(i));
                copy.setEnabled(false);
                MainActivity.objectDB.objectDao().insertObject(copy);
                MainActivity.foregroundAdapterObjects.get(i).setEnabled(false);
            }

            //Log.d("id last fo",MainActivity.foregroundAdapterObjects.get(MainActivity.foregroundAdapterObjects.size()-1).getID() + "");
            // put preset objects in
            SharedPreferences sharedPref = view.getContext().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
            int lastID = sharedPref.getInt(MainActivity.OBJ_PREFS_KEY, 0);
            //Log.d("lastid",lastID + "");
            SharedPreferences.Editor editor = sharedPref.edit();

            ArrayList<RawObject> presetObjects = MainActivity.presets.get(position).getPresetForeground(lastID, editor);

            for(int i = 0; i < presetObjects.size(); i++){
                RawObject r = presetObjects.get(i);
                Log.d("id",r.getId()+"");
                MainActivity.objectDB.objectDao().insertObject(r);
                ForegroundObject foregroundObject = new ForegroundObject(view.getContext(), r.getId(),
                        r.isEnabled(), r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(),
                        r.isChangeOnBounce(), r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());
                MainActivity.foregroundAdapterObjects.add(foregroundObject);
            }

            // Background
            ArrayList<Background> bkgs = (ArrayList<Background>) MainActivity.objectDB.backgroundDao().getBackgrounds();
            //Log.d("bkgDB size", bkgs.size() + "" );
            for (int i = 0; i < bkgs.size(); i++){
                MainActivity.objectDB.backgroundDao().removeBackground(bkgs.get(i));
            }
            Background background = MainActivity.presets.get(position).getPresetBackground();
            MainActivity.objectDB.backgroundDao().insertBackground(background);
            if(MainActivity.backgroundAdapterBackgrounds.size() > 0) {
                MainActivity.backgroundAdapterBackgrounds.set(0,background);
            }
            else{
                MainActivity.backgroundAdapterBackgrounds.add(background);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);

            MainActivity.launchWPServiceAsync async = new MainActivity.launchWPServiceAsync(view.getContext(),
                    MainActivity.objectDB);
            async.execute();

        }
    }

}

