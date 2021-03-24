package com.austinpurtell.wf.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.austinpurtell.wf.MainActivity;
import com.austinpurtell.wf.R;

import java.util.ArrayList;

public class HelpFragment extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_help, container, false);
        setHasOptionsMenu(true);

        HelpFragment.loadHelpAsync loadHelpAsync = new HelpFragment.loadHelpAsync(view);
        loadHelpAsync.execute();

        return view;

    }

    private static class loadHelpAsync extends AsyncTask<Void, Void, ArrayList<Bitmap>> {

        private View view;

        loadHelpAsync(View v){
            this.view = v;
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... voids) {

            ArrayList<Bitmap> bitmaps = new ArrayList<>();

            Bitmap unscaled = BitmapFactory.decodeResource(view.getContext().getResources(), R.raw.wf_help);
            float ar = unscaled.getHeight()/(float)unscaled.getWidth();
            float width = MainActivity.WIDTH * 0.7f;
            Bitmap bitmap = Bitmap.createScaledBitmap(unscaled, (int)width, (int)(width*ar), false);
            bitmaps.add(bitmap);
            bitmaps.add(bitmap);
            bitmaps.add(bitmap);

            return bitmaps;

        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);

            ImageView imageView1 = view.findViewById(R.id.help1);
            imageView1.setImageBitmap(bitmaps.get(0));

            /*LinearLayout faqLayout = view.findViewById(R.id.faq);
            ViewGroup.LayoutParams lp = faqLayout.getLayoutParams();
            lp.width = (int)(MainActivity.WIDTH * 0.7f);
            faqLayout.setLayoutParams(lp);*/
            /*ImageView imageView2 = view.findViewById(R.id.help2);
            imageView2.setImageBitmap(bitmaps.get(1));
            ImageView imageView3 = view.findViewById(R.id.help3);
            imageView3.setImageBitmap(bitmaps.get(2));*/

        }
    }

}
