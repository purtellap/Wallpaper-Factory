package com.austinpurtell.wf.fragments.subfragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.adapters.LibraryGifAdapter;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.database.LibraryGif;
import com.austinpurtell.wf.fragments.BackgroundFragment;
import com.austinpurtell.wf.fragments.LibraryFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class BackgroundGifFragment extends Fragment {

    RecyclerView recyclerView;
    LibraryGifAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    public static int numCols = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.sub_fragment_gif, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view);

        // Add objects
        ImageButton saveButton = (ImageButton) view.findViewById(R.id.saveButton);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        ImageButton addButton = (ImageButton) view.findViewById(R.id.addButton);
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

                                LibraryFragment.clearGifsAsync clearGifsAsync = new LibraryFragment.clearGifsAsync(
                                        view.getContext(), MainActivity.objectDB, adapter);
                                clearGifsAsync.execute();

                                // resets sharedpref to zero to avoid big numbers
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).edit();
                                editor.putInt(MainActivity.GIF_PREFS_KEY, 0);
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
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                builder.setCancelable(false);
                builder.setTitle(v.getContext().getString(R.string.gifLib));
                builder.setMessage(v.getContext().getString(R.string.storedGifInfo));
                builder.setPositiveButton(v.getContext().getString(R.string.OK),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.deleteButton:
                    case R.id.saveButton: {
                        final LibraryGif selectedLibGif = adapter.selectedLibGif;
                        if(selectedLibGif != null){
                            if(v.getId() == R.id.saveButton){
                                setBackgroundGifAsync setBackgroundAsync = new setBackgroundGifAsync(
                                        view, MainActivity.objectDB, selectedLibGif.getGifName());
                                setBackgroundAsync.execute();
                                startBackgroundFragment();
                                adapter.selectedLibGif = null;
                            }
                            else if(v.getId() == R.id.deleteButton){
                                final View view = v;
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                                builder.setCancelable(true);
                                builder.setTitle(view.getContext().getString(R.string.delGifTitle));
                                builder.setMessage(view.getContext().getString(R.string.delGifDesc));
                                builder.setPositiveButton(view.getContext().getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                LibraryGifAdapter.removeGifAsync removeGifAsync = new LibraryGifAdapter.removeGifAsync(
                                                        view, MainActivity.objectDB, adapter, adapter.selectedLibGif);
                                                removeGifAsync.execute();
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
                        }
                        else{
                            Toast.makeText(v.getContext(), v.getContext().getString(R.string.toastNoGifSelected), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case R.id.addButton:
                        if(ContextCompat.checkSelfPermission(v.getContext().getApplicationContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQ_GIF);
                        }
                        else{
                            getGif();
                        }
                        break;
                    case R.id.cancelButton:
                        startBackgroundFragment();
                        adapter.selectedLibGif = null;
                        break;
                }
            }
        };

        saveButton.setOnClickListener(listener);
        addButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);

        layoutManager = new GridLayoutManager(view.getContext(), numCols);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new LibraryGifAdapter(view, recyclerView);
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

    private void startBackgroundFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BackgroundFragment()).addToBackStack(null).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(requestCode == MainActivity.REQ_GIF){
                    getGif();
                }
            }
            else{
                Toast.makeText(this.getActivity(), this.getString(R.string.toastDenied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getGif(){

        Intent intent = new Intent();
        intent.setType("image/gif");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, MainActivity.REQ_GIF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQ_GIF) {
            if (resultCode == RESULT_OK) {
                try {

                    Log.d("data type:",getContext().getContentResolver().getType(data.getData()));
                    String type = getContext().getContentResolver().getType(data.getData());
                    if(type.equals("image/gif")){

                        ProgressBar spinner = (ProgressBar) view.findViewById(R.id.progress);
                        spinner.setVisibility(View.VISIBLE);

                        AssetFileDescriptor gifAsset = getActivity().getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                        FileInputStream in = gifAsset.createInputStream();

                        File dir = new File( getContext().getDir(MainActivity.FOLDER_GIFS, Context.MODE_PRIVATE) + File.separator);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File newfile = new File(dir, MainActivity.PREFIX_GIFS + System.currentTimeMillis() + ".gif");
                        OutputStream out = new FileOutputStream(newfile);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();

                        Log.d("gif saved to", newfile.getAbsolutePath());

                        // put path in library list
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                        int lastID = sharedPref.getInt(MainActivity.GIF_PREFS_KEY, 0);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(MainActivity.GIF_PREFS_KEY, lastID+1);
                        editor.apply();

                        LibraryGif gif = new LibraryGif(lastID+1, newfile.getAbsolutePath());

                        LibraryFragment.putGifAsync putGifAsync = new LibraryFragment.putGifAsync(view.getContext(), MainActivity.objectDB, adapter, recyclerView, gif);
                        putGifAsync.execute();
                    }
                    else{
                        Toast.makeText(getContext(), "Wrong file type selected", Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), this.getString(R.string.toastFail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class setBackgroundGifAsync extends AsyncTask<Void, Void, Void>{

        private View view;
        private AppDatabase database;
        private String fileName;

        setBackgroundGifAsync(View c , AppDatabase db, String fileName){
            this.view = c;
            this.database = db;
            this.fileName = fileName;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // Removes background
            ArrayList<Background> bkgs = (ArrayList<Background>) database.backgroundDao().getBackgrounds();

            for (int i = 0; i < bkgs.size(); i++){
                database.backgroundDao().removeBackground(bkgs.get(i));
            }

            Background background = new Background(0, fileName, -1);
            database.backgroundDao().insertBackground(background);

            MainActivity.backgroundAdapterBackgrounds.set(0,background);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /*final ImageView imageView = (ImageView) view.findViewById(R.id.backgroundImg);

            imageView.setImageBitmap(null);
            Log.d("bkg", MainActivity.backgroundAdapterBackgrounds.size() +"");

            imageView.setBackgroundColor(MainActivity.backgroundAdapterBackgrounds.get(0).getColor());
            if(MainActivity.backgroundAdapterBackgrounds.get(0).usesImage()){
                Bitmap b = MainActivity.backgroundAdapterBackgrounds.get(0).makePreviewImage(view.getContext());
                imageView.setImageBitmap(b);
            }*/

            Toast.makeText( view.getContext(), view.getContext().getString(R.string.toastBkgSet), Toast.LENGTH_SHORT ).show();
        }
    }

}
