package com.austinpurtell.wf.fragments;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.austinpurtell.wf.adapters.ForegroundAdapter;
import com.austinpurtell.wf.adapters.LibraryImageAdapter;
import com.austinpurtell.wf.database.LibraryImage;
import com.austinpurtell.wf.objects.ForegroundObject;
import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.extras.SwipeDeleteCallback;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.RawObject;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class ForegroundFragment extends Fragment {

    Button addObj;
    RecyclerView recyclerView;
    ForegroundAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    CheckBox clearAll;
    CheckBox toggleAll;
    private int lastCode;
    public LibraryImageAdapter editLibAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_foreground, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view);

        // Add objects
        addObj = (Button) view.findViewById(R.id.fab_add_obj);
        addObj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                int lastID = sharedPref.getInt(MainActivity.OBJ_PREFS_KEY, 0);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID+1);
                editor.apply();

                RawObject rawObj = new RawObject(lastID+1);

                ForegroundFragment.putObjAsync putObjectsAsync = new ForegroundFragment.putObjAsync(view.getContext(),
                        MainActivity.objectDB, mAdapter, recyclerView, rawObj);
                putObjectsAsync.execute();

            }
        });

        clearAll = view.findViewById(R.id.clear_button);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                builder.setCancelable(true);
                builder.setTitle(v.getContext().getString(R.string.clearTitle));
                builder.setMessage(v.getContext().getString(R.string.clearObjects));
                builder.setPositiveButton(v.getContext().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ForegroundFragment.clearObjectsAsync clearObjectsAsync = new ForegroundFragment.clearObjectsAsync(
                                        view.getContext(), MainActivity.objectDB, mAdapter);
                                clearObjectsAsync.execute();

                                // resets sharedpref to zero to avoid big numbers
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).edit();
                                editor.putInt(MainActivity.OBJ_PREFS_KEY, 0);
                                editor.apply();

                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ForegroundAdapter(view, recyclerView, this);
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new
                SwipeDeleteCallback(mAdapter, getActivity(), view, recyclerView));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        toggleAll = view.findViewById(R.id.toggle_button);
        toggleAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ForegroundFragment.ToggleAllAsync toggleAllAsync = new ForegroundFragment.ToggleAllAsync(
                        MainActivity.objectDB, mAdapter, toggleAll.isChecked());
                toggleAllAsync.execute();

                //Toast.makeText(v.getContext(), "Toggle All", Toast.LENGTH_SHORT).show();

            }
        });

        // add a default object if first time
        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        int lastID = sharedPref.getInt(MainActivity.OBJ_PREFS_KEY, 0);
        boolean firstTime = sharedPref.getBoolean("firstTime", true);

        if (firstTime) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID+1);
            editor.putBoolean("firstTime", false);
            editor.apply();

            RawObject rawObj = new RawObject(lastID+1);

            ForegroundFragment.putObjAsync putObjectsAsync = new ForegroundFragment.putObjAsync(getContext(),
                    MainActivity.objectDB, mAdapter, recyclerView, rawObj);
            putObjectsAsync.execute();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(requestCode == MainActivity.REQ_IMG_CIRC){
                    startCropper(false, true, null);
                }
                else if(requestCode == MainActivity.REQ_IMG_SQUARE){
                    startCropper(false, false, null);
                }
            }
            else{
                Toast.makeText(this.getActivity(), this.getString(R.string.toastDenied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if(resultUri != null){
                    try{

                        File selectedImgFile = new File(getPathFromUri(resultUri));
                        //Log.d("path", selectedImgFile.getAbsolutePath());

                        Bitmap bitmap = BitmapFactory.decodeFile(selectedImgFile.getAbsolutePath());

                        boolean usesCirc = false;
                        if(lastCode == MainActivity.REQ_IMG_CIRC){
                            makeCircle(getContext(), selectedImgFile, bitmap);
                            usesCirc = true;
                        }

                        // put path in library list
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                        int lastID = sharedPref.getInt(MainActivity.IMG_PREFS_KEY, 0);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(MainActivity.IMG_PREFS_KEY, lastID+1);
                        editor.apply();

                        LibraryImage image = new LibraryImage(lastID+1, selectedImgFile.getAbsolutePath());
                        image.setCircle(usesCirc);

                        LibraryFragment.putImgAsync putObjectsAsync = new LibraryFragment.putImgAsync(view.getContext(),
                                MainActivity.objectDB, editLibAdapter, recyclerView, image);
                        putObjectsAsync.execute();

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), this.getString(R.string.toastFail), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    // Starts android image cropper
    public void startCropper(boolean usesUri, boolean usesCirc, Uri uri){

        CropImage.ActivityBuilder activity;
        if(usesUri){
            activity = CropImage.activity(uri);
        }
        else{
            activity = CropImage.activity();
        }

        // FOREGROUND FRAGMENT CROPPER SETTINGS
        if(!usesCirc){
            activity.setActivityTitle(getContext().getString(R.string.aboutAIC)).
                    setInitialCropWindowPaddingRatio(0).
                    setAllowFlipping(true).setAllowRotation(true).
                    setBackgroundColor(0x00000000).
                    setMultiTouchEnabled(true).
                    setOutputCompressQuality(100).
                    setOutputCompressFormat(Bitmap.CompressFormat.PNG).
                    setAutoZoomEnabled(true).
                    start(getContext(), this);
            lastCode = MainActivity.REQ_IMG_SQUARE;
        }
        else{
            activity.setActivityTitle(getContext().getString(R.string.aboutAIC)).
                    setAllowFlipping(true).setAllowRotation(true).
                    setAspectRatio(1, 1).
                    setBackgroundColor(0x00000000).
                    setMultiTouchEnabled(true).
                    setOutputCompressQuality(100).
                    setOutputCompressFormat(Bitmap.CompressFormat.PNG).
                    setAutoZoomEnabled(true).
                    setCropShape(CropImageView.CropShape.OVAL).
                    start(getContext(), this);
            lastCode = MainActivity.REQ_IMG_CIRC;
        }

    }

    private String getPathFromUri(Uri uri){
        String filePath;
        Cursor cursor = this.getActivity().getContentResolver().query(uri, null, null, null, null);

        if(cursor == null){
            filePath = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }


    static void makeCircle(Context c, File f, Bitmap bitmap) {
        File[] files = c.getCacheDir().listFiles();
        if(files != null)
            for(File file : files) {
                if(file.getAbsolutePath().equals(f.getAbsolutePath())){
                    ContextWrapper cw = new ContextWrapper(c);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(f);
                        /*Log.d("bm path", f.getAbsolutePath());
                        Log.d("bm null", bitmap + "");*/
                        Bitmap b = CropImage.toOvalBitmap(bitmap);
                        b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                }
            }
    }

    public static class putObjAsync extends AsyncTask<Void, Void, RawObject>{

        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private RecyclerView recyclerView;
        private RawObject rawObject;

        public putObjAsync(Context c , AppDatabase db, ForegroundAdapter adapter, RecyclerView recyclerView, RawObject rawObject){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
            this.rawObject = rawObject;
        }

        @Override
        protected RawObject doInBackground(Void... voids) {

            database.objectDao().insertObject(rawObject);

            //Log.d("Put Async","Successful");

            return rawObject;
        }

        @Override
        protected void onPostExecute(RawObject r) {
            super.onPostExecute(r);

            //Log.d("ID", r.getId() + "");

            ForegroundObject foregroundObject = new ForegroundObject(context, r.getId(),
                    r.isEnabled(), r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(),
                    r.isChangeOnBounce(), r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());

            MainActivity.foregroundAdapterObjects.add(foregroundObject);

            //Log.d("size of objects", adapter.objects.size() + "");

            adapter.notifyItemInserted(MainActivity.foregroundAdapterObjects.size() - 1); // questionable
            recyclerView.scrollToPosition(MainActivity.foregroundAdapterObjects.size()-1);
        }
    }

    private static class clearObjectsAsync extends AsyncTask<Void, Void, Void>{

        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;

        clearObjectsAsync(Context c , AppDatabase db, ForegroundAdapter adapter){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Log.d("Clear Async","Successful");

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();
            for (int i = 0; i < rawObjects.size(); i++){
                database.objectDao().removeObject(rawObjects.get(i));
            }

            //Log.d("size of database", database.objectDao().getAllObjects().size() + "");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int size = MainActivity.foregroundAdapterObjects.size();
            for (int i = 0; i < size; i++) {
                MainActivity.foregroundAdapterObjects.remove(0);
                adapter.notifyItemRemoved(0); // ree
            }

            //Log.d("size of objects", adapter.objects.size() + "");
            Toast.makeText( context, "Objects Removed.", Toast.LENGTH_SHORT ).show();
        }
    }

    private static class ToggleAllAsync extends AsyncTask<Void, Void, Void>{

        private AppDatabase database;
        private ForegroundAdapter adapter;
        private boolean toggle;

        ToggleAllAsync(AppDatabase db, ForegroundAdapter adapter, boolean toggle){
            this.database = db;
            this.adapter = adapter;
            this.toggle = toggle;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();
            for (int i = 0; i < rawObjects.size(); i++){

                RawObject copy = rawObjects.get(i);
                database.objectDao().removeObject(rawObjects.get(i));
                copy.setEnabled(toggle);
                database.objectDao().insertObject(copy);
                MainActivity.foregroundAdapterObjects.get(i).setEnabled(toggle);
                /*Log.d("toggleall","copy" + i + " enabled:" + copy.isEnabled());
                Log.d("toggleall","adapter" + i + " enabled:" + adapter.objects.get(i).isEnabled());*/
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter.notifyDataSetChanged();
        }
    }

}
