package com.austinpurtell.wf.fragments;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.austinpurtell.wf.R;

public class AboutFragment extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_about, container, false);
        setHasOptionsMenu(true);

        AboutAsync aboutAsync = new AboutAsync(view);
        aboutAsync.execute();

        return view;

    }

    private static class AboutAsync extends AsyncTask<Void, Void, Void> {

        private View view;

        AboutAsync(View v){
            this.view = v;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Button cropperLink = (Button) view.findViewById(R.id.cropperLink);
            cropperLink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("https://github.com/ArthurHub/Android-Image-Cropper"));
                    view.getContext().startActivity(intent);
                }
            });
            Button cropperApacheLink = (Button) view.findViewById(R.id.cropperApacheLink);
            cropperApacheLink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
                    view.getContext().startActivity(intent);
                }
            });

            Button holoLink = (Button) view.findViewById(R.id.holoLink);
            holoLink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("https://github.com/LarsWerkman/HoloColorPicker"));
                    view.getContext().startActivity(intent);
                }
            });
            Button holoApacheLink = (Button) view.findViewById(R.id.holoApacheLink);
            holoApacheLink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
                    view.getContext().startActivity(intent);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);

            ImageView aboutImg = view.findViewById(R.id.aboutImg);
            aboutImg.setImageBitmap(BitmapFactory.decodeResource(view.getContext().getResources(), R.raw.wf_gear_trim));

        }
    }
}
