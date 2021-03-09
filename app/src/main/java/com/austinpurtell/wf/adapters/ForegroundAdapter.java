package com.austinpurtell.wf.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.austinpurtell.wf.fragments.ForegroundFragment;
import com.austinpurtell.wf.objects.ForegroundObject;
import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.LibraryImage;
import com.austinpurtell.wf.database.RawObject;
import com.austinpurtell.wf.fragments.LibraryFragment;
import com.austinpurtell.wf.objects.PackObject;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ForegroundAdapter extends RecyclerView.Adapter<ForegroundAdapter.MyViewHolder> {

    private ForegroundFragment foregroundFragment;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView layout;
        ImageView imageView;
        TextView nameView;
        TextView desc1View;
        TextView desc2View;
        TextView desc3View;
        TextView desc4View;
        CheckBox checkBox;
        TextView id;

        MyViewHolder(CardView l) {
            super(l);
            layout = l;
            //id = (TextView) l.findViewById(R.id.id);
            imageView = (ImageView) l.findViewById(R.id.objImgPreview);
            desc1View = (TextView) l.findViewById(R.id.objSize);
            desc2View = (TextView) l.findViewById(R.id.objSpeed);
            desc3View = (TextView) l.findViewById(R.id.objAngle);
            checkBox = l.findViewById(R.id.isSelected);
        }
    }

    public ForegroundAdapter(View view, RecyclerView recyclerView, ForegroundFragment fragment) {

        this.foregroundFragment = fragment;
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        ForegroundAdapter.getObjectsAsync getObjectsAsync = new ForegroundAdapter.getObjectsAsync(view.getContext(), MainActivity.objectDB, this, recyclerView);
        getObjectsAsync.execute();
    }

    @Override
    public @NonNull ForegroundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView l = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_foregroundobject, parent, false);
        return new MyViewHolder(l);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        ForegroundObject obj = MainActivity.foregroundAdapterObjects.get(position);
        String sizeString = obj.getSize() + "%";
        String speedString = obj.getSpeed() + "%";
        String angleString = obj.getAngle() + "°";

        // fills single object recycle view
        holder.imageView.setImageBitmap(obj.getImage());
        if (obj.usesColor()) {
            holder.imageView.setImageTintList(ColorStateList.valueOf(obj.getColor()));
        } else {
            holder.imageView.setImageTintList(null);
        }
        holder.desc1View.setText(sizeString);
        holder.desc2View.setText(speedString);
        holder.desc3View.setText(angleString);
        holder.checkBox.setChecked(obj.isEnabled());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.checkBox.setChecked(isChecked);
                doToggleSingleAsync(holder.getAdapterPosition(), isChecked);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // give trigger warning
                SharedPreferences sharedPref =  v.getContext().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
                boolean seenWarning = sharedPref.getBoolean("seenWarning", false);

                if(!seenWarning){
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                    builder.setCancelable(false);
                    builder.setTitle(v.getContext().getString(R.string.seizureWarning));
                    builder.setMessage(v.getContext().getString(R.string.seizureWarningText));
                    builder.setPositiveButton(v.getContext().getString(R.string.warningUnderstand),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("seenWarning", true);

                    editor.apply();

                }

                doEditSingleAsync(v, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return MainActivity.foregroundAdapterObjects.size();
    }

    private void doToggleSingleAsync(int position, boolean isChecked){
        ForegroundAdapter adapter = this;
        ForegroundAdapter.ToggleSingleAsync toggleSingleAsync = new ForegroundAdapter.ToggleSingleAsync(
                MainActivity.objectDB, adapter, position, isChecked);
        toggleSingleAsync.execute();
    }

    private void doEditSingleAsync(View v, int position){
        ForegroundAdapter adapter = this;
        ForegroundAdapter.EditSingleAsync editSingleAsync = new ForegroundAdapter.EditSingleAsync(v, v.getContext(),
                MainActivity.objectDB, adapter, position, this.foregroundFragment);
        editSingleAsync.execute();
    }

    private static class EditSingleAsync extends AsyncTask<Void, Void, RawObject>{

        private View view;
        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private int position;
        private ForegroundFragment foregroundFragment;

        EditSingleAsync(View v, Context c, AppDatabase db, ForegroundAdapter adapter, int position, ForegroundFragment fragment){
            this.view = v;
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.position = position;
            this.foregroundFragment = fragment;
        }

        @Override
        protected RawObject doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();

            int ID = MainActivity.foregroundAdapterObjects.get(position).getID();
            RawObject copy = null;


            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                if(r.getId() == ID){
                    copy = r;
                    break;
                }
            }
            return copy;
        }

        @Override
        protected void onPostExecute(RawObject rawcopy) {
            super.onPostExecute(rawcopy);

            // This is not be the most efficient code but I decided to make a foreground obj out of the raw object.
            // Edit Async reads the object out of the database and makes a copy.  Then Save Async writes it to DB.
            final ForegroundObject copy = new ForegroundObject(context, rawcopy.getId(), rawcopy.isEnabled(),
                    rawcopy.getImageName(), rawcopy.isUsesLibraryImage(), rawcopy.isUseColor(), rawcopy.getColor(), rawcopy.isChangeOnBounce(),
                    rawcopy.getSize(), rawcopy.getSpeed(), rawcopy.getAngle(), rawcopy.usesGravity(), rawcopy.usesShadow(), rawcopy.isFlipXonBounce(), rawcopy.isFlipYonBounce());

            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.popup_obj, null);

            final CheckBox enabledButton = popupView.findViewById(R.id.enabledCB);
            final CheckBox dupButton = popupView.findViewById(R.id.duplicateButton);
            SeekBar sizeSlider = popupView.findViewById(R.id.sbSize);
            SeekBar speedSlider = popupView.findViewById(R.id.sbSpeed);
            SeekBar angleSlider = popupView.findViewById(R.id.sbAngle);
            final TextView sizeText = popupView.findViewById(R.id.sizeText);
            final TextView speedText = popupView.findViewById(R.id.speedText);
            final TextView angleText = popupView.findViewById(R.id.angleText);
            Button defaultButton = popupView.findViewById(R.id.default_button);
            Button imageButton = popupView.findViewById(R.id.img_button);
            final ImageView imageView = popupView.findViewById(R.id.settingsImg);
            Button saveButton = popupView.findViewById(R.id.saveButton);
            Button cancelButton = popupView.findViewById(R.id.cancelButton);

            final Switch cobSwitch = popupView.findViewById(R.id.cobSwitch);
            final Switch useColSwitch = popupView.findViewById(R.id.useColSwitch);
            final Button colorButton = popupView.findViewById(R.id.colorButton);

            final Switch useGravitySwitch = popupView.findViewById(R.id.gravitySwitch);
            final Switch useShadowSwitch = popupView.findViewById(R.id.shadowSwitch);

            final Switch flipXSwitch = popupView.findViewById(R.id.flipX);
            final Switch flipYSwitch = popupView.findViewById(R.id.flipY);

            // Check box
            final Drawable checkTrue = view.getContext().getResources().getDrawable( R.drawable.ic_checkbox_in);
            final Drawable checkFalse = view.getContext().getResources().getDrawable( R.drawable.ic_checkbox_out);

            // header buttons
            enabledButton.setChecked(copy.isEnabled());

            enabledButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    copy.setEnabled(enabledButton.isChecked());

                }
            });

            dupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPref = ((Activity)v.getContext()).getSharedPreferences(MainActivity.PREFERENCES,Context.MODE_PRIVATE);
                    int lastID = sharedPref.getInt(MainActivity.OBJ_PREFS_KEY, 0);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(MainActivity.OBJ_PREFS_KEY, lastID+1);
                    editor.apply();

                    RawObject rawObj = new RawObject(lastID+1, copy.isEnabled(), copy.getImageName(), copy.isUsesLibraryImage(),
                            copy.usesColor(), copy.getColor(), copy.changesOnBounce(), copy.getSize(), copy.getSpeed(),
                            new Random().nextInt(360), copy.usesGravity(), copy.usesShadow(), copy.flipsXonBounce(), copy.flipsYonBounce());

                    ForegroundAdapter.duplicateAsync putObjectsAsync = new ForegroundAdapter.duplicateAsync(view.getContext(),
                            MainActivity.objectDB, adapter, rawObj);
                    putObjectsAsync.execute();
                }
            });

            // image
            imageView.setImageBitmap(copy.getImage());
            if(copy.usesColor()){
                imageView.setImageTintList(ColorStateList.valueOf( copy.getColor() ));
            }
            else{
                imageView.setImageTintList(null);
            }

            // color button
            colorButton.setBackgroundTintList(ColorStateList.valueOf(copy.getColor()));

            colorButton.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), "Change Color", Toast.LENGTH_SHORT).show();
                    LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView = inflater.inflate(R.layout.popup_holo, null);

                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    final ColorPicker picker = (ColorPicker) popupView.findViewById(R.id.picker);
                    SaturationBar saturationBar = (SaturationBar) popupView.findViewById(R.id.saturationbar);
                    ValueBar valueBar = (ValueBar) popupView.findViewById(R.id.valuebar);

                    picker.addSaturationBar(saturationBar);
                    picker.addValueBar(valueBar);

                    picker.setColor(copy.getColor());
                    picker.setOldCenterColor(copy.getColor());

                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    ImageButton butt = popupView.findViewById(R.id.done_button);
                    butt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            int color = picker.getColor();
                            //Log.d("Color:", color + "");
                            colorButton.setBackgroundTintList(ColorStateList.valueOf(color));
                            copy.setColor(color);
                            copy.setUseColor(true);
                            useColSwitch.setChecked(true);
                            imageView.setImageTintList(ColorStateList.valueOf( copy.getColor() ));
                        }
                    });

                    ImageButton cancelButt = popupView.findViewById(R.id.cancelButton);
                    cancelButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                }
            });

            // Switches
            // Cant use COB switch unless you use color
            useColSwitch.setChecked(copy.usesColor());
            if(!copy.usesColor()){
                cobSwitch.setAlpha(0.5f);
                cobSwitch.setClickable(false);
                if(copy.changesOnBounce()){
                    cobSwitch.setChecked(false);
                    copy.setChangeOnBounce(false);
                }
            }
            else{
                cobSwitch.setChecked(copy.changesOnBounce());
            }

            useColSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setUseColor(isChecked);
                    if(!isChecked){
                        cobSwitch.setAlpha(0.5f);
                        cobSwitch.setClickable(false);
                        if(copy.changesOnBounce()){
                            cobSwitch.setChecked(false);
                        }
                        imageView.setImageTintList(null);
                    }
                    else{
                        cobSwitch.setAlpha(1f);
                        cobSwitch.setClickable(true);
                        cobSwitch.setChecked(copy.changesOnBounce());
                        imageView.setImageTintList(ColorStateList.valueOf( copy.getColor() ));
                    }
                }
            });

            cobSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setChangeOnBounce(isChecked);
                }
            });

            // Text and sliders
            final int sizeSliderMin = 10; // %
            sizeSlider.setMax(100-sizeSliderMin);
            angleSlider.setMax(359);

            angleSlider.setProgress(copy.getAngle());
            sizeSlider.setProgress(copy.getSize() - sizeSliderMin);
            speedSlider.setProgress(copy.getSpeed());
            sizeText.setText(view.getContext().getString(R.string.objSize, copy.getSize()));
            speedText.setText(view.getContext().getString(R.string.objVelocity, copy.getSpeed()));
            angleText.setText(view.getContext().getString(R.string.objAngle, copy.getAngle()));

            SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    switch (seekBar.getId()){

                        case (R.id.sbSize):
                            sizeText.setText(view.getContext().getString(R.string.objSize, progress + sizeSliderMin));
                            copy.setSize(progress + sizeSliderMin);
                            break;
                        case (R.id.sbSpeed):
                            speedText.setText(view.getContext().getString(R.string.objVelocity, progress));
                            copy.setSpeed(progress);
                            break;
                        case (R.id.sbAngle):
                            angleText.setText(view.getContext().getString(R.string.objAngle, progress));
                            copy.setAngle(progress);
                            break;

                    }


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

            sizeSlider.setOnSeekBarChangeListener(seekBarListener);
            speedSlider.setOnSeekBarChangeListener(seekBarListener);
            angleSlider.setOnSeekBarChangeListener(seekBarListener);

            //  switches
            useGravitySwitch.setChecked(copy.usesGravity());
            useGravitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setUsesGravity(isChecked);
                }
            });

            useShadowSwitch.setChecked(copy.usesShadow());
            useShadowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setUsesShadow(isChecked);
                }
            });

            flipXSwitch.setChecked(copy.flipsXonBounce());
            flipXSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setFlipXonBounce(isChecked);
                }
            });

            flipYSwitch.setChecked(copy.flipsYonBounce());
            flipYSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    copy.setFlipYonBounce(isChecked);
                }
            });

            // edit buttons
            defaultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), "Switch", Toast.LENGTH_SHORT).show();

                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    final View popupView = inflater.inflate(R.layout.sub_fragment_pack_icons, null);
                    RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(popupView.getContext(), 4);
                    recyclerView.setLayoutManager(layoutManager);

                    final DefaultAdapter adapter = new DefaultAdapter(popupView, recyclerView);
                    recyclerView.setAdapter(adapter);

                    ImageButton saveButton = (ImageButton) popupView.findViewById(R.id.saveButton);
                    ImageButton cancelButton = (ImageButton) popupView.findViewById(R.id.cancelButton);

                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PackObject selectedObject = adapter.selectedObject;
                            if(selectedObject != null){
                                // update copy
                                copy.setUsesLibraryImage(false);
                                copy.setImageName(selectedObject.getImageName());
                                copy.setDefaultImage(v.getContext());
                                imageView.setImageBitmap(copy.getImage());
                                popupWindow.dismiss();
                                adapter.selectedObject = null;
                            }
                            else{
                                Toast.makeText(v.getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            adapter.selectedObject = null;
                        }
                    });

                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


                    /*useColSwitch.setChecked(true);
                    cobSwitch.setChecked(true);
                    copy.changeDefaultImage(v.getContext());
                    imageView.setImageBitmap(copy.getImage());*/
                }
            });

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    /*
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ForegroundLibraryFragment()).addToBackStack(null).commit();*/

                    //Toast.makeText(v.getContext(), "Change Color", Toast.LENGTH_SHORT).show();
                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    final View popupView = inflater.inflate(R.layout.sub_fragment_image, null);
                    RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(popupView.getContext(), LibraryFragment.numCols);
                    recyclerView.setLayoutManager(layoutManager);

                    final LibraryImageAdapter libAdapter = new LibraryImageAdapter(popupView, recyclerView);
                    recyclerView.setAdapter(libAdapter);

                    // cringe
                    foregroundFragment.editLibAdapter = libAdapter;

                    // Add objects
                    ImageButton editButton = (ImageButton) popupView.findViewById(R.id.editButton);
                    ImageButton saveButton = (ImageButton) popupView.findViewById(R.id.saveButton);
                    ImageButton deleteButton = (ImageButton) popupView.findViewById(R.id.deleteButton);
                    ImageButton addSquare = (ImageButton) popupView.findViewById(R.id.addSquareButton);
                    ImageButton addCircle = (ImageButton) popupView.findViewById(R.id.addCircleButton);
                    ImageButton cancelButton = (ImageButton) popupView.findViewById(R.id.cancelButton);
                    CheckBox clearAll = (CheckBox) popupView.findViewById(R.id.clear_button);
                    CheckBox info = (CheckBox) popupView.findViewById(R.id.info_button);

                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    info.setOnClickListener(LibraryFragment.infoClickListener);
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
                                                    view.getContext(), MainActivity.objectDB, libAdapter);
                                            clearObjectsAsync.execute();

                                            // resets sharedpref to zero to avoid big numbers
                                            SharedPreferences.Editor editor = activity.getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE).edit();
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

                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.deleteButton:
                                case R.id.editButton:
                                case R.id.saveButton: {
                                    final LibraryImage selectedLibImg = libAdapter.selectedLibImg;
                                    if(selectedLibImg != null){
                                        if(v.getId() == R.id.saveButton){
                                            useColSwitch.setChecked(false);
                                            // update copy
                                            copy.setUsesLibraryImage(true);
                                            copy.setImageName(selectedLibImg.getImageName());
                                            copy.setLibraryImage(v.getContext());
                                            imageView.setImageBitmap(copy.getImage());
                                            popupWindow.dismiss();
                                            libAdapter.selectedLibImg = null;
                                            imageView.setImageTintList(null);
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
                                                                    view, MainActivity.objectDB, libAdapter, libAdapter.selectedLibImg);
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
                                            foregroundFragment.startCropper(true, selectedLibImg.isCircle(), uri);
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
                                        foregroundFragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQ_IMG_SQUARE);
                                    }
                                    else{
                                        foregroundFragment.startCropper(false, false, null);
                                    }
                                    break;
                                case R.id.addCircleButton:
                                    if(ContextCompat.checkSelfPermission(v.getContext().getApplicationContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                                        foregroundFragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQ_IMG_CIRC);
                                    }
                                    else{
                                        foregroundFragment.startCropper(false, true, null);
                                    }
                                    break;
                                case R.id.cancelButton:
                                    popupWindow.dismiss();
                                    libAdapter.selectedLibImg = null;
                                    if(copy.isUsesLibraryImage()){
                                        //Toast.makeText(view.getContext(), "Deleted current image", Toast.LENGTH_SHORT).show();
                                        copy.setLibraryImage(view.getContext());
                                        imageView.setImageBitmap(copy.getImage());
                                    }
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

                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                }
            });

            // Display to popup window
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            //view.setBackgroundColor(Color.argb(0xaa, 0x00, 0x00, 0x00));
            /*int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;*/
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // save button
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Object Saved", Toast.LENGTH_SHORT).show();

                    RawObject saveCopy = new RawObject(copy.getID(),copy.isEnabled(), copy.getImageName(), copy.isUsesLibraryImage(), copy.usesColor(),
                            copy.getColor(), copy.changesOnBounce(), copy.getSize(), copy.getSpeed(), copy.getAngle(), copy.usesGravity(), copy.usesShadow(),
                            copy.flipsXonBounce(), copy.flipsYonBounce());

                    imageView.clearColorFilter();

                    // update to database
                    ForegroundAdapter.SaveSingleAsync saveSingleAsync = new ForegroundAdapter.SaveSingleAsync(popupWindow, database, adapter, position, copy, saveCopy);
                    saveSingleAsync.execute();

                    //popupWindow.dismiss();
                }
            });

            // cancel button
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

            // Display to popup window
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        }
    }

    private static class SaveSingleAsync extends AsyncTask<Void, Void, Void>{

        private PopupWindow window;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private ForegroundObject saveCopy;
        private RawObject rawSaveCopy;
        private int position;

        SaveSingleAsync(PopupWindow w, AppDatabase db, ForegroundAdapter adapter, int position, ForegroundObject saveCopy, RawObject rawSaveCopy){
            this.window = w;
            this.database = db;
            this.adapter = adapter;
            this.position = position;
            this.saveCopy = saveCopy;
            this.rawSaveCopy = rawSaveCopy;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();

            int ID = MainActivity.foregroundAdapterObjects.get(position).getID();

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                if(r.getId() == ID){

                    // removes current one in db and replaces with new version
                    database.objectDao().removeObject(rawObjects.get(i));
                    database.objectDao().insertObject(rawSaveCopy);
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MainActivity.foregroundAdapterObjects.set(position, saveCopy);
            adapter.notifyItemChanged(position);

            window.dismiss();

            //ForegroundAdapter.UpdateObjectsAsync updateObjectsAsync = new ForegroundAdapter.UpdateObjectsAsync(context, database, adapter);
            //updateObjectsAsync.execute();

        }
    }

    private static class getObjectsAsync extends AsyncTask<Void, Void, ArrayList<ForegroundObject>> {

        private Context context;
        private ArrayList<ForegroundObject> asyncObjects = new ArrayList<>();
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private RecyclerView recyclerView;

        getObjectsAsync(Context c, AppDatabase db, ForegroundAdapter adapter, RecyclerView recyclerView){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected ArrayList<ForegroundObject> doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();

            //Log.d("size of database", rawObjects.size() + "");

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                //Log.d("imgName getobjects",r.getPathName());
                ForegroundObject obj = new ForegroundObject(context, r.getId(), r.isEnabled(),
                        r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(), r.isChangeOnBounce(),
                        r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());
                asyncObjects.add(obj);
                //Log.i("Object",obj.toString());
            }

            return asyncObjects;
        }

        @Override
        protected void onPostExecute(ArrayList<ForegroundObject> asyncObjects) {
            super.onPostExecute(asyncObjects);
            MainActivity.foregroundAdapterObjects = asyncObjects;

            //Log.d("size of objects", adapter.objects.size() + "");

            for (int i = 0; i < MainActivity.foregroundAdapterObjects.size(); i++){
                //Log.d("ID", adapter.objects.get(i).getID() + "");
                //adapter.notifyItemInserted(i); // questionable
                recyclerView.scrollToPosition(0);
            }

            ProgressBar spinner = (ProgressBar) recyclerView.getRootView().findViewById(R.id.progress);
            spinner.setVisibility(View.GONE);
        }
    }

    private static class ToggleSingleAsync extends AsyncTask<Void, Void, Void>{

        private AppDatabase database;
        private ForegroundAdapter adapter;
        private int position;
        private boolean enabled;

        ToggleSingleAsync(AppDatabase db, ForegroundAdapter adapter, int position, boolean e){
            this.database = db;
            this.adapter = adapter;
            this.position = position;
            this.enabled = e;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();

            int ID = MainActivity.foregroundAdapterObjects.get(position).getID();

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                //Log.d("DB ID ", r.getId() + "");
                if(r.getId() == ID){

                    RawObject copy = rawObjects.get(i);
                    copy.setEnabled(enabled);
                    database.objectDao().removeObject(rawObjects.get(i));
                    database.objectDao().insertObject(copy);
                    MainActivity.foregroundAdapterObjects.get(i).setEnabled(enabled);
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter.notifyItemChanged(position);
        }
    }

    public static class duplicateAsync extends AsyncTask<Void, Void, RawObject>{

        private Context context;
        private AppDatabase database;
        private ForegroundAdapter adapter;
        private RawObject rawObject;

        public duplicateAsync(Context c, AppDatabase db, ForegroundAdapter adapter, RawObject rawObject){
            this.context = c;
            this.database = db;
            this.adapter = adapter;
            this.rawObject = rawObject;
        }

        @Override
        protected RawObject doInBackground(Void... voids) {

            database.objectDao().insertObject(rawObject);

            return rawObject;
        }

        @Override
        protected void onPostExecute(RawObject r) {
            super.onPostExecute(r);

            ForegroundObject foregroundObject = new ForegroundObject(context, r.getId(), r.isEnabled(), r.getImageName(),
                    r.isUsesLibraryImage(), r.isUseColor(), r.getColor(), r.isChangeOnBounce(), r.getSize(), r.getSpeed(),
                    r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());

            MainActivity.foregroundAdapterObjects.add(foregroundObject);

            adapter.notifyItemInserted(MainActivity.foregroundAdapterObjects.size() - 1);
        }
    }

}

