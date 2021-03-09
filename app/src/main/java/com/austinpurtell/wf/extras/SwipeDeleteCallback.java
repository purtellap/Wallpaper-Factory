package com.austinpurtell.wf.extras;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import com.austinpurtell.wf.objects.ForegroundObject;
import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.adapters.ForegroundAdapter;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.RawObject;
import com.austinpurtell.wf.fragments.ForegroundFragment;

import java.util.ArrayList;

public class SwipeDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private ForegroundAdapter myAdapter;
    private Drawable icon;
    private final ColorDrawable background;
    private View view;
    private RecyclerView recyclerView;

    public SwipeDeleteCallback(ForegroundAdapter adapter, Context context, View v, RecyclerView recyclerView){

        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        myAdapter = adapter;
        view = v;

        icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        icon.setTint(context.getColor(R.color.off_white));
        background = new ColorDrawable(context.getColor(R.color.colorPrimary));
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // used for up and down movements
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        //TextView textView = viewHolder.itemView.findViewById(R.id.objID);
        //int ID = Integer.parseInt(textView.getText().toString());

        SwipeDeleteCallback.removeObjAsync removeObjAsync = new SwipeDeleteCallback.removeObjAsync(view, view.getContext(), MainActivity.objectDB, myAdapter, viewHolder.getAdapterPosition(), recyclerView);
        removeObjAsync.execute();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20; //so background is behind the rounded corners of itemView

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            int iconRight = itemView.getLeft() + iconMargin;
            icon.setBounds(iconRight, iconTop, iconLeft, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

    public static class removeObjAsync extends AsyncTask<Void, Void, ForegroundObject> {

        private View view;
        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private int position;
        private RecyclerView recyclerView;

        public removeObjAsync(View v, Context c , AppDatabase db, ForegroundAdapter adapter, int position, RecyclerView recyclerView){
            this.view = v;
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.position = position;
            this.recyclerView = recyclerView;
        }

        @Override
        protected ForegroundObject doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();

            int ID = MainActivity.foregroundAdapterObjects.get(position).getID();

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                if(r.getId() == ID){
                    database.objectDao().removeObject(r);

                    ForegroundObject removedObj = MainActivity.foregroundAdapterObjects.get(i);
                    MainActivity.foregroundAdapterObjects.remove(i);

                    //Log.d("size of database", database.objectDao().getAllObjects().size() + "");
                    //Log.d("size of objects", adapter.objects.size() + "");

                    return removedObj;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ForegroundObject f) {
            super.onPostExecute(f);

            adapter.notifyItemRemoved(position);

            Snackbar snackbar = Snackbar.make(view, "Object Removed.",
                    Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RawObject rawObj = new RawObject(f.getID(),f.isEnabled(), f.getImageName(), f.isUsesLibraryImage(), f.usesColor(),
                            f.getColor(), f.changesOnBounce(), f.getSize(), f.getSpeed(), f.getAngle(), f.usesGravity(), f.usesShadow(), f.flipsXonBounce(), f.flipsYonBounce());

                    ForegroundFragment.putObjAsync putObjectsAsync = new ForegroundFragment.putObjAsync(view.getContext(),
                            MainActivity.objectDB, adapter, recyclerView, rawObj);
                    putObjectsAsync.execute();

                }
            });
            View snackbarView = snackbar.getView();
            snackbarView.setPadding(5,5,5,5);
            snackbar.show();
        }
    }
}
