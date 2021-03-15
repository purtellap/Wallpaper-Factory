package com.austinpurtell.wf.fragments;

import android.Manifest;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.adapters.LibraryGifAdapter;
import com.austinpurtell.wf.adapters.LibraryImageAdapter;
import com.austinpurtell.wf.adapters.LibraryVideoAdapter;
import com.austinpurtell.wf.database.LibraryGif;
import com.austinpurtell.wf.database.LibraryImage;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.LibraryVideo;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.austinpurtell.wf.adapters.LibraryGifAdapter.clearAllGifs;
import static com.austinpurtell.wf.adapters.LibraryImageAdapter.clearAllCacheFiles;
import static com.austinpurtell.wf.adapters.LibraryVideoAdapter.clearAllVideos;

public class LibraryFragment extends Fragment {

    FloatingActionButton addObj;
    RecyclerView recyclerView;
    LibraryImageAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    public static int numCols = 3;
    private int lastCode;

    public static View.OnClickListener infoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
            builder.setCancelable(false);
            builder.setTitle(v.getContext().getString(R.string.nav_Library));
            builder.setMessage(v.getContext().getString(R.string.storedInfo));
            builder.setPositiveButton(v.getContext().getString(R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_library, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.library_recycler_view);

        // Add objects
        ImageButton editButton = (ImageButton) view.findViewById(R.id.editButton);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        ImageButton addSquare = (ImageButton) view.findViewById(R.id.addSquareButton);
        ImageButton addCircle = (ImageButton) view.findViewById(R.id.addCircleButton);

        CheckBox clearAll = (CheckBox) view.findViewById(R.id.clear_button);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                builder.setCancelable(true);
                builder.setTitle(v.getContext().getString(R.string.clearTitle));
                builder.setMessage(v.getContext().getString(R.string.clearDesc));
                builder.setPositiveButton(v.getContext().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                clearImagesAsync clearObjectsAsync = new clearImagesAsync(
                                        view.getContext(), MainActivity.objectDB, mAdapter);
                                clearObjectsAsync.execute();

                                // resets sharedpref to zero to avoid big numbers
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).edit();
                                editor.putInt("lastImgID", 0);
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

        CheckBox info = view.findViewById(R.id.info_button);
        info.setOnClickListener(infoClickListener);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.deleteButton:
                    case R.id.editButton:
                    case R.id.saveButton: {
                        final LibraryImage selectedLibImg = mAdapter.selectedLibImg;
                        if(selectedLibImg != null){
                            if(v.getId() == R.id.deleteButton){
                                final View view = v;
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                                builder.setCancelable(true);
                                builder.setTitle(view.getContext().getString(R.string.delTitle));
                                builder.setMessage(view.getContext().getString(R.string.delDesc));
                                builder.setPositiveButton(view.getContext().getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                LibraryImageAdapter.removeImgAsync removeImgAsync = new LibraryImageAdapter.removeImgAsync(
                                                        view, MainActivity.objectDB, mAdapter, mAdapter.selectedLibImg);
                                                removeImgAsync.execute();
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
                            else if(v.getId() == R.id.editButton){
                                Uri uri = Uri.fromFile(new File(selectedLibImg.getImageName()));
                                startCropper(true, selectedLibImg.isCircle(), uri);
                            }
                        }
                        else{
                            Toast.makeText(v.getContext(), v.getContext().getString(R.string.toastNoneSelected), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case R.id.addSquareButton:
                        if(ContextCompat.checkSelfPermission(v.getContext().getApplicationContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQ_IMG_SQUARE);
                        }
                        else{
                            startCropper(false, false, null);
                        }
                        break;
                    case R.id.addCircleButton:
                        if(ContextCompat.checkSelfPermission(v.getContext().getApplicationContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQ_IMG_CIRC);
                        }
                        else{
                            startCropper(false, true, null);
                        }
                        break;
                }
            }
        };

        editButton.setOnClickListener(listener);
        addCircle.setOnClickListener(listener);
        addSquare.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);

        layoutManager = new GridLayoutManager(view.getContext(), numCols);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new LibraryImageAdapter(view, recyclerView);
        recyclerView.setAdapter(mAdapter);

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
                        Log.d("path", selectedImgFile.getAbsolutePath());

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

                        putImgAsync putObjectsAsync = new putImgAsync(view.getContext(),
                                MainActivity.objectDB, mAdapter, recyclerView, image);
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
        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if(resultUri != null){
                    try{

                        File selectedImgFile = new File(getPathFromUri(resultUri));
                        Log.d("path", selectedImgFile.getAbsolutePath());
                        //Log.d("image name wo path", selectedImgFile.getName());

                        // add pic to user photos hopefully
                        //Bitmap bitmap = BitmapFactory.decodeFile(selectedImgFile.getAbsolutePath());
                        //saveImageToExternal(bitmap, selectedImgFile.getName());
                        //new ImageSaver(getContext()).setExternal(true).setFileName(selectedImgFile.getName()).setDirectoryName("wallpaperfactory").save(bitmap);

                        // save image to pictures
                        //ContentResolver resolver = this.getActivity().getContentResolver();
                        //File newpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/wallpaperfactory");
                        //Log.d("new path", newpath.getAbsolutePath());
                        //MediaStore.Images.Media.insertImage(resolver,newpath.getAbsolutePath(), selectedImgFile.getAbsolutePath(), null);

                       *//* MediaStore.Images.Media.insertImage(resolver, selectedImgFile.getAbsolutePath(), selectedImgFile.getName(), null);
                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedImgFile)));*//*

                        //String imageGalleryPath = insertImage(resolver, bitmap, selectedImgFile.getName(), "");
                        *//*String imageGalleryPath = Environment.getExternalStorageDirectory()+ File.separator +
                                Environment.DIRECTORY_PICTURES + File.separator + "wallpaperfactory";*//*
                        //Log.d("imageGalleryPath", imageGalleryPath);

                        // put path in library list
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                        int lastID = sharedPref.getInt("lastImgID", 0);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("lastImgID", lastID+1);
                        editor.apply();

                        LibraryImage image = new LibraryImage(lastID+1, selectedImgFile.getAbsolutePath());

                        LibraryFragment.putObjAsync putObjectsAsync = new LibraryFragment.putObjAsync(view.getContext(),
                                MainActivity.objectDB, mAdapter, recyclerView, image);
                        putObjectsAsync.execute();


                    }
                    catch (Exception e){
                        Toast.makeText(this.getActivity(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }*/
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

    public static class putImgAsync extends AsyncTask<Void, Void, LibraryImage>{

        private Context context;
        private AppDatabase database;
        private LibraryImageAdapter adapter;
        private RecyclerView recyclerView;
        private LibraryImage libraryImage;

        public putImgAsync(Context c , AppDatabase db, LibraryImageAdapter adapter, RecyclerView recyclerView, LibraryImage i){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
            this.libraryImage = i;
        }

        @Override
        protected LibraryImage doInBackground(Void... voids) {

            libraryImage.setImage(context);

            database.libraryImageDao().insertObject(libraryImage);

            //Log.d("Put Async","Successful");

            return libraryImage;
        }

        @Override
        protected void onPostExecute(LibraryImage i) {
            super.onPostExecute(i);

            //Log.d("ID", r.getId() + "");

            MainActivity.libraryAdapterImages.add(i);

            //Log.d("size of objects", adapter.objects.size() + "");

            adapter.notifyItemInserted(MainActivity.libraryAdapterImages.size() - 1);
            recyclerView.scrollToPosition(MainActivity.libraryAdapterImages.size() - 1);
        }
    }

    public static class putGifAsync extends AsyncTask<Void, Void, LibraryGif>{

        private Context context;
        private AppDatabase database;
        private LibraryGifAdapter adapter;
        private RecyclerView recyclerView;
        private LibraryGif libraryGif;

        public putGifAsync(Context c , AppDatabase db, LibraryGifAdapter adapter, RecyclerView recyclerView, LibraryGif i){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
            this.libraryGif = i;
        }

        @Override
        protected LibraryGif doInBackground(Void... voids) {

            libraryGif.setGif();

            database.libraryGifDao().insertObject(libraryGif);

            //Log.d("Put Async","Successful");

            return libraryGif;
        }

        @Override
        protected void onPostExecute(LibraryGif i) {
            super.onPostExecute(i);

            //Log.d("ID", r.getId() + "");

            MainActivity.libraryAdapterGifs.add(i);

            //Log.d("size of objects", adapter.objects.size() + "");
            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);


            adapter.notifyItemInserted(MainActivity.libraryAdapterGifs.size() - 1);
            recyclerView.scrollToPosition(MainActivity.libraryAdapterGifs.size() - 1);
        }
    }

    public static class putVideoAsync extends AsyncTask<Void, Void, LibraryVideo>{

        private Context context;
        private AppDatabase database;
        private LibraryVideoAdapter adapter;
        private RecyclerView recyclerView;
        private LibraryVideo libraryVideo;

        public putVideoAsync(Context c , AppDatabase db, LibraryVideoAdapter adapter, RecyclerView recyclerView, LibraryVideo i){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
            this.libraryVideo = i;
        }

        @Override
        protected LibraryVideo doInBackground(Void... voids) {

            //libraryVideo.setImage(context);

            database.libraryVideoDao().insertObject(libraryVideo);

            //Log.d("Put Async","Successful");

            return libraryVideo;
        }

        @Override
        protected void onPostExecute(LibraryVideo i) {
            super.onPostExecute(i);

            //Log.d("ID", r.getId() + "");

            MainActivity.libraryAdapterVideos.add(i);

            //Log.d("size of objects", adapter.objects.size() + "");
            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);

            adapter.notifyItemInserted(MainActivity.libraryAdapterVideos.size() - 1);
            recyclerView.scrollToPosition(MainActivity.libraryAdapterVideos.size() - 1);
        }
    }

    public static class clearImagesAsync extends AsyncTask<Void, Void, Void>{

        private Context context;
        private AppDatabase database;
        private LibraryImageAdapter adapter;

        public clearImagesAsync(Context c , AppDatabase db, LibraryImageAdapter adapter){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Log.d("Clear Async","Successful");

            ArrayList<LibraryImage> images = (ArrayList<LibraryImage>) database.libraryImageDao().getAllImages();
            for (int i = 0; i < images.size(); i++){
                database.libraryImageDao().removeObject(images.get(i));
            }

            //Log.d("size of database", database.objectDao().getAllObjects().size() + "");
            clearAllCacheFiles(context);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int size = MainActivity.libraryAdapterImages.size();
            for (int i = 0; i < size; i++) {
                MainActivity.libraryAdapterImages.remove(0);
                adapter.notifyItemRemoved(0);
            }

            adapter.selectedLibImg = null;

            //Log.d("size of objects", adapter.objects.size() + "");
            Toast.makeText( context, "Images Removed.", Toast.LENGTH_SHORT ).show();
        }
    }

    public static class clearGifsAsync extends AsyncTask<Void, Void, Void>{

        private Context context;
        private AppDatabase database;
        private LibraryGifAdapter adapter;

        public clearGifsAsync(Context c , AppDatabase db, LibraryGifAdapter adapter){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<LibraryGif> gifs = (ArrayList<LibraryGif>) database.libraryGifDao().getAllGifs();
            for (int i = 0; i < gifs.size(); i++){
                database.libraryGifDao().removeObject(gifs.get(i));
            }

            clearAllGifs(context);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int size = MainActivity.libraryAdapterGifs.size();
            for (int i = 0; i < size; i++) {
                MainActivity.libraryAdapterGifs.remove(0);
                adapter.notifyItemRemoved(0);
            }

            adapter.selectedLibGif = null;

            Toast.makeText( context, R.string.toastGifsRemoved, Toast.LENGTH_SHORT ).show();
        }
    }


    public static class clearVideosAsync extends AsyncTask<Void, Void, Void>{

        private Context context;
        private AppDatabase database;
        private LibraryVideoAdapter adapter;

        public clearVideosAsync(Context c , AppDatabase db, LibraryVideoAdapter adapter){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<LibraryVideo> videos = (ArrayList<LibraryVideo>) database.libraryVideoDao().getAllVideos();
            for (int i = 0; i < videos.size(); i++){
                database.libraryVideoDao().removeObject(videos.get(i));
            }

            clearAllVideos(context);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int size = MainActivity.libraryAdapterVideos.size();
            for (int i = 0; i < size; i++) {
                MainActivity.libraryAdapterVideos.remove(0);
                adapter.notifyItemRemoved(0);
            }

            adapter.selectedLibVid = null;

            Toast.makeText( context, R.string.toastVidsRemoved, Toast.LENGTH_SHORT ).show();
        }
    }

    /* public void saveImageToExternal(Bitmap bm, String n) {

        String imageFileName = n.substring(0, n.length() - 4) + ".png";

        //Create Path to save Image
        String savePath = Environment.DIRECTORY_PICTURES + "/baconreader";
        File d = Environment.getExternalStoragePublicDirectory(savePath); //Creates app specific folder
        Log.d("new path", d.getAbsolutePath());
        // make the directory pls
        if(!d.exists()){
            boolean mkdir = d.mkdirs();
            Log.d("mkdirs", mkdir +"");
        }

        //Files.createDirectory(path.toPath());
        File imageFile = new File(d, imageFileName);
        Log.d("imagefile",imageFile.getAbsolutePath());

        // save to different place in internal storage
   *//*     ContextWrapper cw = new ContextWrapper(this.getContext());
        File directory = cw.getDir(savePath, Context.MODE_PRIVATE);
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
        }*//*

        try{
            FileOutputStream out = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this.getContext(),new String[] { imageFile.getAbsolutePath() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/

   /* public String insertImage(ContentResolver cr, Bitmap source, String t, String description) {

        String title = "WF_" + t.substring(7, t.length() - 4) + ".png";
        String path = createDirectoryAndSaveFile(source, title);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        // whyyyyyyy
        //values.put(MediaStore.Images.Media.DATA, path);
        values.put(MediaStore.MediaColumns.DATA, path);

        Uri url = null;
        String stringUrl = null;    *//* value to be returned *//*

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.PNG, 100, imageOut);
                } finally {
                    imageOut.close();
                }

                //long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                //Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                //storeThumbnail(cr, miniThumb, id, 50F, 50F,MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }*/

   /* private static String createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "wallpaperfactory");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }*/


        /*private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }*/

        /*    private void saveImageToDirectory(Bitmap finalBitmap, String n) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        Log.d("Root",root);
        File myDir = new File(root + "/" + R.string.img_directory);
        if(!myDir.mkdirs()){
            Log.d("Error", "couldn't make directory");
        }

        String imageFileName = n.substring(0, n.length() - 4) + ".png";
        Log.d("imageFIleName",imageFileName);
        File file = new File (myDir, imageFileName);
        if (file.exists ()){
            file.delete ();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this.getActivity(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }*/

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainActivity.REQ_SELECT && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        InputStream inputStream = this.getActivity().getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //imageView.setImageBitmap(bitmap);

                        File selectedImgFile = new File(getPathFromUri(selectedImageUri));
                        Log.d("path", selectedImgFile.getPath());

                        // put path in library list
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                        int lastID = sharedPref.getInt("lastImgID", 0);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("lastImgID", lastID+1);
                        editor.apply();

                        LibraryImage image = new LibraryImage(lastID+1, selectedImgFile.getAbsolutePath());

                        LibraryFragment.putObjAsync putObjectsAsync = new LibraryFragment.putObjAsync(view.getContext(),
                        MainActivity.objectDB, mAdapter, recyclerView, image);
                        putObjectsAsync.execute();


                    }
                    catch (Exception e){
                        Toast.makeText(this.getActivity(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        }

    }*/

/*    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }*/

}
