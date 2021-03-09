package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.austinpurtell.wf.objects.PackObject;
import com.austinpurtell.wf.objects.Packs;

import java.util.ArrayList;

public class DefaultAdapter extends RecyclerView.Adapter<DefaultAdapter.MyViewHolder> {

    View v;
    public PackObject selectedObject = null;
    RecyclerView recyclerView;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        CardView cardView;
        ImageView image;
        ImageView overlay;
        ImageView gradient;
        ConstraintLayout constraintLayout;

        MyViewHolder(ConstraintLayout l, final DefaultAdapter adapter) {
            super(l);
            layout = l;
            image = (ImageView) l.findViewById(R.id.defImgPreview);
            cardView = (CardView) l.findViewById(R.id.defaultCV);
            overlay = (ImageView) l.findViewById(R.id.lockedOverlay);
            constraintLayout = l.findViewById(R.id.constraintLayout);
            gradient = l.findViewById(R.id.gradientF);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(MainActivity.packObjects.get(getAdapterPosition()).isUnlocked()){
                        adapter.selectedObject = MainActivity.packObjects.get(getAdapterPosition());
                        adapter.update(getAdapterPosition());
                        image.getDrawable().setColorFilter(v.getResources().getColor(R.color.selectedDefaultColor, v.getContext().getTheme()), PorterDuff.Mode.SRC_ATOP);
                    }
                    else{
                        adapter.selectedObject = null;
                        adapter.update(getAdapterPosition());
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.toastPackLocked), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ProgressBar spinner = (ProgressBar) adapter.recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);
        }

    }

    public DefaultAdapter(View view, RecyclerView recyclerView) {
        this.v = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        this.recyclerView = recyclerView;

        DefaultAdapter.getObjectsAsync getObjectsAsync = new getObjectsAsync(this, recyclerView, v.getContext());
        getObjectsAsync.execute();

    }

    @Override
    public @NonNull DefaultAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ConstraintLayout l = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_defaults, parent, false);
        return new MyViewHolder(l, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.image.setImageBitmap( MainActivity.packObjects.get(holder.getAdapterPosition()).getImage());
        if(!MainActivity.packObjects.get(holder.getAdapterPosition()).isUnlocked()){
            holder.overlay.setImageTintMode(PorterDuff.Mode.SRC_IN);
            holder.gradient.setVisibility(View.VISIBLE);
            holder.overlay.setVisibility(View.VISIBLE);
        }

    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void update(int position){
        for(int i = 0; i < MainActivity.packObjects.size(); i++){
            if(i != position){
                notifyItemChanged(i);
            }
        }

    }

    public static class getObjectsAsync extends AsyncTask<Void, Void, ArrayList<PackObject>> {

        private DefaultAdapter adapter;
        private RecyclerView recyclerView;
        private MyViewHolder holder;
        private Context context;

        getObjectsAsync(DefaultAdapter adapter, RecyclerView recyclerView, Context c){

            this.adapter = adapter;
            this.recyclerView = recyclerView;
            this.holder = holder;
            this.context = c;
        }

        @Override
        protected ArrayList<PackObject> doInBackground(Void... voids) {

            return Packs.getAllObjects(context);

        }

        @Override
        protected void onPostExecute(ArrayList<PackObject> asyncObjs) {
            super.onPostExecute(asyncObjs);
            MainActivity.packObjects = asyncObjs;

            for (int i = 0; i < MainActivity.packObjects.size(); i++){

                recyclerView.scrollToPosition(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.packObjects.size();
    }

}

