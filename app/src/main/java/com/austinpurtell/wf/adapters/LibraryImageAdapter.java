package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.LibraryImage;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.graphics.BitmapFactory.decodeFile;

public class LibraryImageAdapter extends RecyclerView.Adapter<LibraryImageAdapter.MyViewHolder> {

    View view;
    public LibraryImage selectedLibImg = null;
    public int selectedPosition;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        CardView cardView;

        MyViewHolder(CardView l) {
            super(l);
            image = (ImageView) l.findViewById(R.id.libImg);
            cardView = (CardView) l.findViewById(R.id.cardView);
        }

    }

    public LibraryImageAdapter(View view, RecyclerView recyclerView) {
        this.view = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        LibraryImageAdapter.getImagesAsync getImagesAsync = new LibraryImageAdapter.getImagesAsync(MainActivity.objectDB, this, recyclerView);
        getImagesAsync.execute();
    }

    @Override
    public @NonNull LibraryImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView l = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_image, parent, false);

        final MyViewHolder holder = new MyViewHolder(l);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLibImg = MainActivity.libraryAdapterImages.get(holder.getAdapterPosition());
                update(holder.getAdapterPosition());
                holder.image.getDrawable().setColorFilter(0x44ffffff, PorterDuff.Mode.SRC_ATOP);
                selectedPosition = holder.getAdapterPosition();
            }
        });

        holder.image.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                // also do what onlclick does
                selectedLibImg = MainActivity.libraryAdapterImages.get(holder.getAdapterPosition());
                update(holder.getAdapterPosition());
                holder.image.getDrawable().setColorFilter(0x44ffffff, PorterDuff.Mode.SRC_ATOP);
                selectedPosition = holder.getAdapterPosition();

                LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                final View popupView = inflater.inflate(R.layout.popup_img_detail_view, null);

                ImageView imageView = popupView.findViewById(R.id.imgDetailView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(selectedLibImg.getImageName()));

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
                });


                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        final Bitmap bmp = MainActivity.libraryAdapterImages.get(holder.getAdapterPosition()).getImage();
        final ImageView i = holder.image;

        if(MainActivity.libraryAdapterImages.get(holder.getAdapterPosition()).isCircle()){
            holder.cardView.setRadius(1000f);
        }
        /*i.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(new Rect(10,10,b.getWidth() - 20, b.getHeight() + 10), 4);
            }
        });*/

       /*
       final Bitmap b = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
       Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setShadowLayer(8,4,4,0xff000000);
        c.drawBitmap(bmp, 0, 0, p);
        i.setImageBitmap(b);*/
       i.setImageBitmap(bmp);

    }

    @Override
    public int getItemCount() {
        return MainActivity.libraryAdapterImages.size();
    }

    private void update(int position){
        for(int i = 0; i < MainActivity.libraryAdapterImages.size(); i++){
            if(i != position){
                notifyItemChanged(i);
            }
        }

    }

    public static void clearAllCacheFiles(Context c) {
        File[] files = c.getCacheDir().listFiles();
        if(files != null)
            for(File file : files) {
                file.delete();
            }
    }

    public static void clearCacheFile(Context c, String path) {
        File[] files = c.getCacheDir().listFiles();
        if(files != null)
            for(File file : files) {
                if(file.getAbsolutePath().equals(path)){
                    file.delete();
                }
            }
    }

    private static class getImagesAsync extends AsyncTask<Void, Void, ArrayList<LibraryImage>> {

        private AppDatabase database;
        private LibraryImageAdapter adapter;
        private RecyclerView recyclerView;

        getImagesAsync(AppDatabase db, LibraryImageAdapter adapter, RecyclerView recyclerView){

            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected ArrayList<LibraryImage> doInBackground(Void... voids) {

            ArrayList<LibraryImage> asyncImages = (ArrayList<LibraryImage>)  database.libraryImageDao().getAllImages();
            for(int i = 0; i < asyncImages.size(); i++){
                asyncImages.get(i).setImage(recyclerView.getContext());
            }
            return asyncImages;

        }

        @Override
        protected void onPostExecute(ArrayList<LibraryImage> asyncImages) {
            super.onPostExecute(asyncImages);
            MainActivity.libraryAdapterImages = asyncImages;

            //Log.d("size of objects", adapter.objects.size() + "");

            for (int i = 0; i < MainActivity.libraryAdapterImages.size(); i++){
                //Log.d("ID", adapter.objects.get(i).getID() + "");
                //adapter.notifyItemInserted(i); // questionable
                recyclerView.scrollToPosition(0);
            }

            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);
        }
    }

    public static class removeImgAsync extends AsyncTask<Void, Void, LibraryImage> {

        private View view;
        private AppDatabase database;
        private LibraryImageAdapter adapter;
        private LibraryImage selected;

        public removeImgAsync(View v, AppDatabase db, LibraryImageAdapter adapter, LibraryImage i){
            this.view = v;
            this.database = db;
            this.adapter = adapter;
            this.selected = i; // MainActivity.libraryAdapterImages.get(position)
        }

        @Override
        protected LibraryImage doInBackground(Void... voids) {

            ArrayList<LibraryImage> asyncImages = (ArrayList<LibraryImage>) database.libraryImageDao().getAllImages();

            int ID = selected.getId();

            for (int i = 0; i < asyncImages.size(); i++){
                LibraryImage r = asyncImages.get(i);
                if(r.getId() == ID){

                    database.libraryImageDao().removeObject(r);

                    LibraryImage removedImg = MainActivity.libraryAdapterImages.get(i);
                    MainActivity.libraryAdapterImages.remove(i);
                    clearCacheFile(view.getContext(), removedImg.getImageName());

                    return removedImg;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final LibraryImage i) {
            super.onPostExecute(i);

            adapter.notifyItemRemoved(adapter.selectedPosition);

            adapter.selectedLibImg = null;

            Toast.makeText(view.getContext(), view.getContext().getString(R.string.toastImgRemoved), Toast.LENGTH_SHORT).show();

            /*Snackbar snackbar = Snackbar.make(view, "Object Removed.",
                    Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LibraryFragment.putObjAsync putObjectsAsync = new LibraryFragment.putObjAsync(
                            v.getContext(), database, adapter, recyclerView, i);
                    putObjectsAsync.execute();

                }
            });
            View snackbarView = snackbar.getView();
            snackbarView.setPadding(5,5,5,5);
            snackbar.show();

            */
        }
    }

}

