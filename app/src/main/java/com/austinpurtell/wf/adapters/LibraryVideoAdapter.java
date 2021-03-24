package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.LibraryVideo;

import java.io.File;
import java.util.ArrayList;

public class LibraryVideoAdapter extends RecyclerView.Adapter<LibraryVideoAdapter.MyViewHolder> {

    View view;
    public LibraryVideo selectedLibVid = null;
    public int selectedPosition;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        VideoView videoView;
        ConstraintLayout videoContainer;
        ImageView textView;

        MyViewHolder(CardView l) {
            super(l);
            videoView = (VideoView) l.findViewById(R.id.libVideo);
            cardView = (CardView) l.findViewById(R.id.cardView);
            videoContainer = (ConstraintLayout) l.findViewById(R.id.videoContainer);
            textView = (ImageView) l.findViewById(R.id.selectedVideo);

            /*MediaController controller = new MediaController(l.getContext());
            controller.setVisibility(View.GONE);
            controller.setMediaPlayer(videoView);
            videoView.setMediaController(controller);*/
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0f,0f);
                    mp.setLooping(true);
                    videoView.start();
                    //videoView.seekTo(1);
                }
            });
            /*videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    videoView.stopPlayback();
                }
            });*/
            /*videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d("videoview", "surface created");
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d("videoview", "surface changed");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d("videoview", "surface destroyed");
                    videoView.stopPlayback();
                }
            });*/
        }

    }

    public LibraryVideoAdapter(View view, RecyclerView recyclerView) {
        this.view = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        LibraryVideoAdapter.getVideosAsync getVideosAsync = new LibraryVideoAdapter.getVideosAsync(MainActivity.objectDB, this, recyclerView);
        getVideosAsync.execute();
    }

    @Override
    public @NonNull LibraryVideoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView l = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_video, parent, false);

        final MyViewHolder holder = new MyViewHolder(l);

        holder.videoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(holder.videoView.isPlaying()){
                    holder.videoView.pause();
                }
                else{
                    holder.videoView.start();
                }*/
                //holder.videoContainer.setForegroundTintList(ColorStateList.valueOf(0x77000000));
                selectedLibVid = MainActivity.libraryAdapterVideos.get(holder.getAdapterPosition());
                update(holder);
                //holder.textView.getDrawable().setColorFilter(0xaa0000ff, PorterDuff.Mode.SRC_ATOP);
                holder.textView.getDrawable().setColorFilter(0x77ffffff, PorterDuff.Mode.SRC_ATOP);
                //holder.textView.setVisibility(View.INVISIBLE);
                selectedPosition = holder.getAdapterPosition();
                //Log.d("click", "registered");
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        String path = MainActivity.libraryAdapterVideos.get(holder.getAdapterPosition()).getVideoName();
        //Log.d("size", MainActivity.libraryAdapterVideos.size() + "");

        holder.textView.setImageDrawable(view.getContext().getDrawable( R.drawable.gradient_videoview));
        holder.videoView.setVideoPath(path);
        //holder.videoView.setForegroundTintList(ColorStateList.valueOf(0xaaffffff));
    }

    @Override
    public int getItemCount() {
        return MainActivity.libraryAdapterVideos.size();
    }

    private void update(MyViewHolder holder){
        for(int i = 0; i < MainActivity.libraryAdapterVideos.size(); i++){
            if(i != holder.getAdapterPosition()){
                //this.bindViewHolder(holder, holder.getAdapterPosition());
                notifyItemChanged(i);
            }
        }
    }

    public static void clearAllVideos(Context c) {
        File[] files = c.getDir(MainActivity.FOLDER_VIDS, Context.MODE_PRIVATE).listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void clearVideo(Context c, String path) {
        File[] files = c.getDir(MainActivity.FOLDER_VIDS, Context.MODE_PRIVATE).listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getAbsolutePath().equals(path)) {
                    file.delete();
                }
            }
        }
    }

    private static class getVideosAsync extends AsyncTask<Void, Void, ArrayList<LibraryVideo>> {

        private AppDatabase database;
        private LibraryVideoAdapter adapter;
        private RecyclerView recyclerView;

        getVideosAsync(AppDatabase db, LibraryVideoAdapter adapter, RecyclerView recyclerView){

            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected ArrayList<LibraryVideo> doInBackground(Void... voids) {

            return (ArrayList<LibraryVideo>)  database.libraryVideoDao().getAllVideos();
        }

        @Override
        protected void onPostExecute(ArrayList<LibraryVideo> asyncVideos) {
            super.onPostExecute(asyncVideos);
            MainActivity.libraryAdapterVideos = asyncVideos;

            for (int i = 0; i < MainActivity.libraryAdapterVideos.size(); i++){
                recyclerView.scrollToPosition(i);
            }

            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);
        }
    }

    public static class removeVideoAsync extends AsyncTask<Void, Void, LibraryVideo> {

        private View view;
        private AppDatabase database;
        private LibraryVideoAdapter adapter;
        private LibraryVideo selected;

        public removeVideoAsync(View v, AppDatabase db, LibraryVideoAdapter adapter, LibraryVideo i){
            this.view = v;
            this.database = db;
            this.adapter = adapter;
            this.selected = i; // MainActivity.libraryAdapterVideos.get(position)
        }

        @Override
        protected LibraryVideo doInBackground(Void... voids) {

            ArrayList<LibraryVideo> asyncVideos = (ArrayList<LibraryVideo>) database.libraryVideoDao().getAllVideos();

            int ID = selected.getId();

            for (int i = 0; i < asyncVideos.size(); i++){
                LibraryVideo r = asyncVideos.get(i);
                if(r.getId() == ID){

                    database.libraryVideoDao().removeObject(r);

                    LibraryVideo removedImg = MainActivity.libraryAdapterVideos.get(i);
                    MainActivity.libraryAdapterVideos.remove(i);
                    clearVideo(view.getContext(), removedImg.getVideoName());

                    return removedImg;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final LibraryVideo i) {
            super.onPostExecute(i);

            adapter.notifyItemRemoved(adapter.selectedPosition);

            adapter.selectedLibVid = null;

            Toast.makeText(view.getContext(), view.getContext().getString(R.string.toastImgRemoved), Toast.LENGTH_SHORT).show();

        }
    }

}

