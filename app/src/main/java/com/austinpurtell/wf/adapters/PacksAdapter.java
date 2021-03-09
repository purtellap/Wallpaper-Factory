package com.austinpurtell.wf.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.objects.PackObject;
import com.austinpurtell.wf.objects.Packs;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PacksAdapter extends RecyclerView.Adapter<PacksAdapter.MyViewHolder> {

    View view;
    RecyclerView recyclerView;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView layout;
        TextView title;
        ImageView promoImage;
        TextView price;
        ImageView lockedIcon;

        MyViewHolder(CardView cardView, final PacksAdapter adapter, Context context) {
            super(cardView);
            layout = cardView;

            title = layout.findViewById(R.id.packTitle);
            promoImage = layout.findViewById(R.id.promoImage);
            lockedIcon = layout.findViewById(R.id.lockedIcon);
            price = layout.findViewById(R.id.packPrice);


            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView = inflater.inflate(R.layout.popup_pack, null);

                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    final TextView title = (TextView) popupView.findViewById(R.id.packHeader);
                    final TextView priceText = (TextView) popupView.findViewById(R.id.packPrice);

                    Button buyButton = popupView.findViewById(R.id.buyButton);
                    Button cancel = popupView.findViewById(R.id.cancelButton);

                    title.setText(MainActivity.packs.get(getAdapterPosition()).getPromoTitle());
                    if(MainActivity.packs.get(getAdapterPosition()).isUnlocked()){
                        priceText.setVisibility(View.GONE);
                        buyButton.setText(R.string.use);
                    }

                    buyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });


                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    // Background and Icons
                    ImageView imageView = popupView.findViewById(R.id.bkgImg);
                    /*float ar = MainActivity.HEIGHT/(float)MainActivity.WIDTH;
                    float w = imageView.getWidth();
                    imageView.getLayoutParams().height = (int)(w*ar);
                    imageView.requestLayout();*/
                    imageView.setBackgroundColor(Color.argb(0xFF, 0x11, 0x11, 0x11));

                    ImageView icon1 = popupView.findViewById(R.id.icon1);
                    ImageView icon2 = popupView.findViewById(R.id.icon2);

                    icon1.setImageBitmap(MainActivity.packObjects.get(0).getImage());
                    icon2.setImageBitmap(MainActivity.packObjects.get(1).getImage());

                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                }
            });
        }

    }

    public PacksAdapter(View view, RecyclerView recyclerView) {
        this.view = view;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        this.recyclerView = recyclerView;

        PacksAdapter.getPacksAsync getPacksAsync = new PacksAdapter.getPacksAsync(view, recyclerView, this);
        getPacksAsync.execute();

        PacksAdapter.getObjectsAsync getObjectsAsync = new PacksAdapter.getObjectsAsync(view.getContext());
        getObjectsAsync.execute();
    }

    @Override
    public @NonNull PacksAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_packs, parent, false);
        return new MyViewHolder(cardView, this, view.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.title.setText(MainActivity.packs.get(holder.getAdapterPosition()).getPromoTitle());
        holder.promoImage.setImageBitmap(MainActivity.packs.get(holder.getAdapterPosition()).getPromoImage());

        if(MainActivity.packs.get(holder.getAdapterPosition()).isUnlocked()){
            holder.lockedIcon.setVisibility(View.INVISIBLE);
            holder.price.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return MainActivity.packs.size();
    }

    private static class getPacksAsync extends AsyncTask<Void, Void, ArrayList<Packs.Pack>> {

        private View view;
        private RecyclerView recyclerView;
        private PacksAdapter adapter;

        getPacksAsync(View v, RecyclerView recyclerView, PacksAdapter adapter){
            //this.holder = holder;
            this.view = v;
            this.recyclerView = recyclerView;
            this.adapter = adapter;
        }

        @Override
        protected ArrayList<Packs.Pack> doInBackground(Void... voids) {

            return Packs.getPacksForPromo(view.getContext());

        }

        @Override
        protected void onPostExecute(ArrayList<Packs.Pack> asyncObjs) {
            super.onPostExecute(asyncObjs);

            MainActivity.packs = asyncObjs;

            ProgressBar spinner = (ProgressBar) adapter.recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);

            for (int i = 0; i < MainActivity.packs.size(); i++){
                //Log.d("ID", adapter.objects.get(i).getID() + "");
                recyclerView.scrollToPosition(i);
            }

        }
    }

    public static class getObjectsAsync extends AsyncTask<Void, Void, ArrayList<PackObject>> {


        private Context context;

        getObjectsAsync(Context c){

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
        }
    }

    /*private static class usePackAsync extends AsyncTask<Void, Void, Void> {

        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private RecyclerView recyclerView;

        public usePackAsync(Context c , AppDatabase db, ForegroundAdapter adapter, RecyclerView recyclerView){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SharedPreferences sharedPref = ((AppCompatActivity)context).getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
            int lastID = sharedPref.getInt(MainActivity.OBJ_PREFS_KEY, 0);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID+1);
            editor.apply();


            database.objectDao().insertObject(rawObject);

            //Log.d("Put Async","Successful");

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);

            //Log.d("ID", r.getId() + "");

            ForegroundObject foregroundObject = new ForegroundObject(context, r.getId(),
                    r.isEnabled(), r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(),
                    r.isChangeOnBounce(), r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());

            MainActivity.foregroundAdapterObjects.add(foregroundObject);

            //Log.d("size of objects", adapter.objects.size() + "");

            adapter.notifyItemInserted(MainActivity.foregroundAdapterObjects.size() - 1); // questionable
            recyclerView.scrollToPosition(MainActivity.foregroundAdapterObjects.size() - 1);
        }
    }*/

}

