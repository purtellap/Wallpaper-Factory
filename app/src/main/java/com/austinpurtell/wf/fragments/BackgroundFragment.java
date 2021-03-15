package com.austinpurtell.wf.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;
import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.extras.GifImageView;
import com.austinpurtell.wf.fragments.subfragments.BackgroundGifFragment;
import com.austinpurtell.wf.fragments.subfragments.BackgroundImageFragment;
import com.austinpurtell.wf.fragments.subfragments.BackgroundVideoFragment;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class BackgroundFragment extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_background, container, false);
        setHasOptionsMenu(true);

        Button setImage = (Button) view.findViewById(R.id.set_image);
        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLibFragment();
            }
        });

        Button setGif = (Button) view.findViewById(R.id.set_gif);
        setGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGifFragment();
            }
        });

        Button setVideo = (Button) view.findViewById(R.id.set_video);
        setVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVidFragment();
            }
        });

        BackgroundFragment.getBackgroundAsync getBackgroundAsync = new BackgroundFragment.getBackgroundAsync(
                MainActivity.objectDB, view);
        getBackgroundAsync.execute();

        return view;

    }

    private void startLibFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BackgroundImageFragment()).addToBackStack(null).commit();
    }

    private void startGifFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BackgroundGifFragment()).addToBackStack(null).commit();
    }

    private void startVidFragment(){

        SharedPreferences sharedPref =  view.getContext().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        boolean seenWarning = sharedPref.getBoolean("seenVideoAlert", false);

        if(!seenWarning){
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.AlertDialogCustom));
            builder.setCancelable(false);
            builder.setTitle(view.getContext().getString(R.string.videoAlertTitle));
            builder.setMessage(view.getContext().getString(R.string.videoAlert));
            builder.setPositiveButton(view.getContext().getString(R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("seenVideoAlert", true);
            editor.apply();

        }

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BackgroundVideoFragment()).addToBackStack(null).commit();
    }

    private static class getBackgroundAsync extends AsyncTask<Void, Void, Void>{

        private View view;
        private AppDatabase database;
        private Bitmap b;

        getBackgroundAsync (AppDatabase db, View v){
            this.database = db;
            this.view = v;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<Background> bs = (ArrayList<Background>) database.backgroundDao().getBackgrounds();
            if(bs.size() > 0) {
                MainActivity.backgroundAdapterBackgrounds = bs;
            }
            else{
                Background background = new Background(0, Color.argb(0xff, 0x11, 0x11, 0x11));
                database.backgroundDao().insertBackground(background);
                MainActivity.backgroundAdapterBackgrounds.add(background);
            }

            if(MainActivity.backgroundAdapterBackgrounds.get(0).usesImage()){
                b = MainActivity.backgroundAdapterBackgrounds.get(0).makePreviewImage(view.getContext());
            }
            else if (MainActivity.backgroundAdapterBackgrounds.get(0).usesGif()){
                MainActivity.backgroundAdapterBackgrounds.get(0).setGif(view.getResources());
            }

            // ui?
            CheckBox infoButton = (CheckBox) view.findViewById(R.id.info_button);
            /*SeekBar sizeSlider = (SeekBar) view.findViewById(R.id.sizeSB);
            final TextView sizeText = (TextView) view.findViewById(R.id.sizeText);*/

            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                    builder.setCancelable(false);
                    builder.setTitle(v.getContext().getString(R.string.bkgInfoTitle));
                    builder.setMessage(v.getContext().getString(R.string.bkgInfoDesc) + "\n " + MainActivity.HEIGHT + " x " + MainActivity.WIDTH + " " + v.getContext().getString(R.string.bkgInfoDesc2));
                    builder.setPositiveButton(v.getContext().getString(R.string.bkgInfoOK),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            Button setColor  = (Button) view.findViewById(R.id.set_color);
            setColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

                    picker.setColor(MainActivity.backgroundAdapterBackgrounds.get(0).getColor());
                    picker.setOldCenterColor(MainActivity.backgroundAdapterBackgrounds.get(0).getColor());
                    //picker.setShowOldCenterColor(false);

                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    ImageButton butt = popupView.findViewById(R.id.done_button);
                    butt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int color = picker.getColor();
                            popupWindow.dismiss();
                            setBackgroundColorAsync setBackgroundAsync = new setBackgroundColorAsync(
                                    view, MainActivity.objectDB, color);
                            setBackgroundAsync.execute();
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

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Left side
            final GifImageView gifImageView = (GifImageView) view.findViewById(R.id.backgroundGif);
            final VideoView videoView = (VideoView) view.findViewById(R.id.backgroundVideo);
            TextView previewText = (TextView) view.findViewById(R.id.previewText);

            previewText.setVisibility(View.VISIBLE);

            final ImageView imageView = (ImageView) view.findViewById(R.id.backgroundImg);

            // make imageview scaled to the same shape as the screen
            float ar = MainActivity.HEIGHT/(float)MainActivity.WIDTH;
            float width = imageView.getWidth();

            imageView.getLayoutParams().height = (int)(width*ar);
            imageView.requestLayout();

            imageView.setBackgroundColor(MainActivity.backgroundAdapterBackgrounds.get(0).getColor());
            if(MainActivity.backgroundAdapterBackgrounds.get(0).usesImage()){
                imageView.setImageBitmap(b);
            }
            else if (MainActivity.backgroundAdapterBackgrounds.get(0).usesGif()){
                gifImageView.getLayoutParams().height = (int)(width*ar);
                gifImageView.requestLayout();
                gifImageView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                Movie gif = MainActivity.backgroundAdapterBackgrounds.get(0).getGif();
                gifImageView.setGVfromLibGif(gif, gif.width(), gif.height(), 0L);
            }
            else if (MainActivity.backgroundAdapterBackgrounds.get(0).usesVideo()){
                videoView.getLayoutParams().height = (int)(width*ar);
                videoView.requestLayout();
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                videoView.setVideoPath(MainActivity.backgroundAdapterBackgrounds.get(0).getPathName());
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setVolume(0f,0f);
                        mp.setLooping(true);
                        videoView.start();
                    }
                });
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        if(mp.isPlaying()){
                            mp.stop();
                        }
                        mp.reset();
                        mp.release();
                        mp = null;
                    }
                });
                MainActivity.videoViews.add(videoView);
            }

            /*imageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                @Override
                public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                }
            });
            imageView.getCroppedImageAsync();*/

            /*final int sizeSliderMin = 10; // %
            sizeSlider.setMax(200-sizeSliderMin);

            sizeSlider.setProgress(MainActivity.backgroundAdapterBackgrounds.get(0).getSize() - sizeSliderMin);
            //speedSlider.setProgress(copy.getSpeed());
            sizeText.setText(view.getContext().getString(R.string.bkgSize, MainActivity.backgroundAdapterBackgrounds.get(0).getSize()));

            SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    switch (seekBar.getId()){

                        case (R.id.sizeSB):
                            sizeText.setText(view.getContext().getString(R.string.bkgSize, progress + sizeSliderMin));
                            MainActivity.backgroundAdapterBackgrounds.get(0).setSize(progress + sizeSliderMin);
                            imageView.setImageBitmap(MainActivity.backgroundAdapterBackgrounds.get(0).makePreviewImage(view.getContext()));

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

            sizeSlider.setOnSeekBarChangeListener(seekBarListener);*/

        }
    }

    public static class setBackgroundColorAsync extends AsyncTask<Void, Void, Void>{

        private View view;
        private AppDatabase database;
        private int color;

        setBackgroundColorAsync(View c , AppDatabase db, int color){
            this.view = c;
            this.database = db;
            this.color = color;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // Removes background
            ArrayList<Background> bkgs = (ArrayList<Background>) database.backgroundDao().getBackgrounds();

            Log.d("bkgDB size", bkgs.size() + "" );
            for (int i = 0; i < bkgs.size(); i++){
                database.backgroundDao().removeBackground(bkgs.get(i));
            }

            Background background = new Background(0,color);
            database.backgroundDao().insertBackground(background);

            MainActivity.backgroundAdapterBackgrounds.set(0,background);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            final ImageView imageView = (ImageView) view.findViewById(R.id.backgroundImg);
            final VideoView videoView = (VideoView) view.findViewById(R.id.backgroundVideo);
            final GifImageView gifImageView = (GifImageView) view.findViewById(R.id.backgroundGif);

            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            gifImageView.setVisibility(View.GONE);

            imageView.setImageBitmap(null);

            Log.d("bkg", MainActivity.backgroundAdapterBackgrounds.size() +"");

            imageView.setBackgroundColor(MainActivity.backgroundAdapterBackgrounds.get(0).getColor());

            Toast.makeText( view.getContext(), view.getContext().getString(R.string.toastBkgSet), Toast.LENGTH_SHORT ).show();
        }
    }
}
