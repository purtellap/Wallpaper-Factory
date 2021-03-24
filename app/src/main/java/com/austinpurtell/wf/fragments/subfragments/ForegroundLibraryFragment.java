package com.austinpurtell.wf.fragments.subfragments;

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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.adapters.LibraryImageAdapter;
import com.austinpurtell.wf.database.LibraryImage;
import com.austinpurtell.wf.fragments.ForegroundFragment;
import com.austinpurtell.wf.fragments.LibraryFragment;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

import static android.app.Activity.RESULT_OK;

public class ForegroundLibraryFragment extends Fragment {

    RecyclerView recyclerView;
    LibraryImageAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    public static int numCols = 3;
    private int lastCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.sub_fragment_image, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view);

        // Add objects
        ImageButton editButton = (ImageButton) view.findViewById(R.id.editButton);
        ImageButton saveButton = (ImageButton) view.findViewById(R.id.saveButton);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        ImageButton addSquare = (ImageButton) view.findViewById(R.id.addSquareButton);
        ImageButton addCircle = (ImageButton) view.findViewById(R.id.addCircleButton);
        ImageButton cancelButton = (ImageButton) view.findViewById(R.id.cancelButton);

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

                                LibraryFragment.clearImagesAsync clearObjectsAsync = new LibraryFragment.clearImagesAsync(
                                        view.getContext(), MainActivity.objectDB, adapter);
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

        CheckBox info = (CheckBox) view.findViewById(R.id.info_button);
        info.setOnClickListener(LibraryFragment.infoClickListener);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.deleteButton:
                    case R.id.editButton:
                    case R.id.saveButton: {
                        final LibraryImage selectedLibImg = adapter.selectedLibImg;
                        if(selectedLibImg != null){
                            if(v.getId() == R.id.saveButton){
                                /*ForegroundLibraryFragment.setBackgroundColorAsync setBackgroundColorAsync = new ForegroundLibraryFragment.setBackgroundColorAsync(
                                        view, MainActivity.objectDB, selectedLibImg.getPathName());
                                setBackgroundColorAsync.execute();*/
                                startForegroundFragment();
                                adapter.selectedLibImg = null;
                            }
                            else if(v.getId() == R.id.deleteButton){
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
                                                        view, MainActivity.objectDB, adapter, adapter.selectedLibImg);
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
                    case R.id.cancelButton:
                        startForegroundFragment();
                        adapter.selectedLibImg = null;
                        break;
                }
            }
        };

        editButton.setOnClickListener(listener);
        saveButton.setOnClickListener(listener);
        addCircle.setOnClickListener(listener);
        addSquare.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);

        layoutManager = new GridLayoutManager(view.getContext(), numCols);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new LibraryImageAdapter(view, recyclerView);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Hide toolbar for this fragment
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    private void startForegroundFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ForegroundFragment()).addToBackStack(null).commit();
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
                                MainActivity.objectDB, adapter, recyclerView, image);
                        putObjectsAsync.execute();

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(this.getActivity(), this.getString(R.string.toastFail), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
