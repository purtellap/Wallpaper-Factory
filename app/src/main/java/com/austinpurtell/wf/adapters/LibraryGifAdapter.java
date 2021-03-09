package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.LibraryGif;
import com.austinpurtell.wf.extras.GifImageView;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class LibraryGifAdapter extends RecyclerView.Adapter<LibraryGifAdapter.MyViewHolder> {

    View view;
    public LibraryGif selectedLibGif = null;
    public int selectedPosition;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        GifImageView gifImageView;
        ConstraintLayout container;

        MyViewHolder(CardView l) {
            super(l);
            image = (ImageView) l.findViewById(R.id.selectedGif);
            gifImageView = (GifImageView) l.findViewById(R.id.libGif);
            container = (ConstraintLayout) l.findViewById(R.id.gifContainer);
        }

    }

    public LibraryGifAdapter(View view, RecyclerView recyclerView) {
        this.view = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        LibraryGifAdapter.getGifsAsync getGifsAsync = new LibraryGifAdapter.getGifsAsync(MainActivity.objectDB, this, recyclerView);
        getGifsAsync.execute();
    }

    @Override
    public @NonNull LibraryGifAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView l = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_gif, parent, false);

        final MyViewHolder holder = new MyViewHolder(l);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLibGif = MainActivity.libraryAdapterGifs.get(holder.getAdapterPosition());
                update(holder.getAdapterPosition());
                holder.image.getDrawable().setColorFilter(0x77ffffff, PorterDuff.Mode.SRC_ATOP);
                selectedPosition = holder.getAdapterPosition();
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                // also do what onlclick does
                selectedLibGif = MainActivity.libraryAdapterGifs.get(holder.getAdapterPosition());
                update(holder.getAdapterPosition());
                holder.image.getDrawable().setColorFilter(0x77ffffff, PorterDuff.Mode.SRC_ATOP);
                selectedPosition = holder.getAdapterPosition();

                LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                /*final View popupView = inflater.inflate(R.layout.popup_img_detail_view, null);

                ImageView imageView = popupView.findViewById(R.id.imgDetailView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(selectedLibGif.getGifName()));

                RelativeLayout layout = (RelativeLayout) popupView.findViewById(R.id.imgDetailLayout);

                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                final AppCompatActivity activity = (AppCompatActivity) view.getContext();
                popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });*/


                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        holder.image.setImageDrawable(view.getContext().getDrawable( R.drawable.gradient_videoview));
        LibraryGif gif = MainActivity.libraryAdapterGifs.get(holder.getAdapterPosition());
        holder.gifImageView.setGVfromLibGif(gif.getGif(), gif.getWidth(), gif.getHeight(), 0L);
    }

    @Override
    public int getItemCount() {
        return MainActivity.libraryAdapterGifs.size();
    }

    private void update(int position){
        for(int i = 0; i < MainActivity.libraryAdapterGifs.size(); i++){
            if(i != position){
                notifyItemChanged(i);
            }
        }

    }

    public static void clearAllGifs(Context c) {
        File[] files = c.getDir(MainActivity.FOLDER_GIFS, Context.MODE_PRIVATE).listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void clearGif(Context c, String path) {
        File[] files = c.getDir(MainActivity.FOLDER_GIFS, Context.MODE_PRIVATE).listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.getAbsolutePath().equals(path)) {
                    file.delete();
                }
            }
        }
    }


    private static class getGifsAsync extends AsyncTask<Void, Void, ArrayList<LibraryGif>> {

        private AppDatabase database;
        private LibraryGifAdapter adapter;
        private RecyclerView recyclerView;

        getGifsAsync(AppDatabase db, LibraryGifAdapter adapter, RecyclerView recyclerView){

            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected ArrayList<LibraryGif> doInBackground(Void... voids) {

            ArrayList<LibraryGif> asyncGifs = (ArrayList<LibraryGif>)  database.libraryGifDao().getAllGifs();
            /*for(int i = 0; i < asyncGifs.size(); i++){
                asyncGifs.get(i).setImage(recyclerView.getContext());
            }*/
            for(LibraryGif gif : asyncGifs){
                gif.setGif();
            }
            return asyncGifs;

        }

        @Override
        protected void onPostExecute(ArrayList<LibraryGif> asyncGifs) {
            super.onPostExecute(asyncGifs);
            MainActivity.libraryAdapterGifs = asyncGifs;

            //Log.d("size of objects", adapter.objects.size() + "");

            for (int i = 0; i < MainActivity.libraryAdapterGifs.size(); i++){
                //Log.d("ID", adapter.objects.get(i).getID() + "");
                //adapter.notifyItemInserted(i); // questionable
                recyclerView.scrollToPosition(i);
            }

            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);
        }
    }

    public static class removeGifAsync extends AsyncTask<Void, Void, LibraryGif> {

        private View view;
        private AppDatabase database;
        private LibraryGifAdapter adapter;
        private LibraryGif selected;

        public removeGifAsync(View v, AppDatabase db, LibraryGifAdapter adapter, LibraryGif i){
            this.view = v;
            this.database = db;
            this.adapter = adapter;
            this.selected = i; // MainActivity.libraryAdapterImages.get(position)
        }

        @Override
        protected LibraryGif doInBackground(Void... voids) {

            ArrayList<LibraryGif> asyncGifs = (ArrayList<LibraryGif>) database.libraryGifDao().getAllGifs();

            int ID = selected.getId();

            for (int i = 0; i < asyncGifs.size(); i++){
                LibraryGif r = asyncGifs.get(i);
                if(r.getId() == ID){

                    database.libraryGifDao().removeObject(r);

                    LibraryGif removedGif = MainActivity.libraryAdapterGifs.get(i);
                    MainActivity.libraryAdapterGifs.remove(i);
                    clearGif(view.getContext(), removedGif.getGifName());

                    return removedGif;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final LibraryGif i) {
            super.onPostExecute(i);

            adapter.notifyItemRemoved(adapter.selectedPosition);

            adapter.selectedLibGif = null;

            Toast.makeText(view.getContext(), view.getContext().getString(R.string.toastImgRemoved), Toast.LENGTH_SHORT).show();

        }
    }

}

