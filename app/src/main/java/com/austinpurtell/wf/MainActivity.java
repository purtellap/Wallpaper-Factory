package com.austinpurtell.wf;

import android.Manifest;
import android.app.WallpaperManager;
import androidx.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import com.austinpurtell.wf.database.AppDatabase;
import com.austinpurtell.wf.database.Background;
import com.austinpurtell.wf.database.LibraryGif;
import com.austinpurtell.wf.database.LibraryImage;
import com.austinpurtell.wf.database.LibraryVideo;
import com.austinpurtell.wf.database.RawObject;
import com.austinpurtell.wf.fragments.AboutFragment;
import com.austinpurtell.wf.fragments.BackgroundFragment;
import com.austinpurtell.wf.fragments.ForegroundFragment;
import com.austinpurtell.wf.fragments.HelpFragment;
import com.austinpurtell.wf.fragments.HomeFragment;
import com.austinpurtell.wf.fragments.SupportFragment;
import com.austinpurtell.wf.objects.ForegroundObject;
import com.austinpurtell.wf.objects.PackObject;
import com.austinpurtell.wf.objects.Packs;
import com.austinpurtell.wf.objects.Presets;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static int HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    NavigationView navigationView;
    public static AppDatabase objectDB;

    public static final String PREFERENCES = "wf_preferences";
    public static final String DB_NAME = "wf_DB";
    public static final String OBJ_PREFS_KEY = "lastObjID";
    public static final String IMG_PREFS_KEY = "lastImgID";
    public static final String GIF_PREFS_KEY = "lastGifID";
    public static final String VID_PREFS_KEY = "lastVidID";
    public static final String FRAGMENT_PREFS_KEY = "lastFragment";
    public static final String FOLDER_VIDS = "wf_videos";
    public static final String PREFIX_VIDS = "wf_video_";
    public static final String FOLDER_GIFS = "wf_gifs";
    public static final String PREFIX_GIFS = "wf_gif_";
    public static final String FOLDER_PRESET_VIDS = "wf_presets";

    public static final int ID_HOME = 0;
    public static final int ID_UPGRADE = 1;
    public static final int ID_FOREGROUND = 2;
    public static final int ID_BACKGROUND = 3;
    public static final int ID_LIBRARY = 4;
    public static final int ID_HELP = 5;
    public static final int ID_ABOUT = 6;
    public static final int ID_SUPPORT = 7;

    public static final int REQ_WP = 1;
    public static final int REQ_IMG_CIRC = 2;
    public static final int REQ_IMG_SQUARE = 3;
    public static final int REQ_GIF = 4;
    public static final int REQ_VIDEO = 5;

    public static ArrayList<ForegroundObject> foregroundAdapterObjects = new ArrayList<>();
    public static ArrayList<Background> backgroundAdapterBackgrounds = new ArrayList<>();
    public static ArrayList<LibraryImage> libraryAdapterImages = new ArrayList<>();
    public static ArrayList<LibraryGif> libraryAdapterGifs = new ArrayList<>();
    public static ArrayList<LibraryVideo> libraryAdapterVideos = new ArrayList<>();
    public static ArrayList<PackObject> packObjects = new ArrayList<>();
    public static ArrayList<Packs.Pack> packs = new ArrayList<>();
    public static ArrayList<Presets.Preset> presets = new ArrayList<>();

    public static ArrayList<VideoView> videoViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // gets w/h of screen including navbar
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getRealSize(size);

        if (size.y > size.x) {
            HEIGHT = size.y;
            WIDTH = size.x;
        } else {
            HEIGHT = size.x;
            WIDTH = size.y;
        }

        super.onCreate(savedInstanceState);

        setTitle("");

        Drawable background = getResources().getDrawable(R.drawable.gradient_main, getTheme());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
        if(Build.VERSION.SDK_INT >= 26){
            getWindow().setFormat(PixelFormat.RGBA_F16);
        }
        getWindow().setBackgroundDrawable(background);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        /*BlurView blurView = (BlurView) findViewById(R.id.blur_vieww);
        blurView.setBlurredView(navigationView);*/

        navigationView.setNavigationItemSelectedListener(this);

        objectDB = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();

        // get last fragment
        SharedPreferences sharedPref =  getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        int fragmentID = sharedPref.getInt(FRAGMENT_PREFS_KEY, ID_HOME);
        checkFragment(fragmentID, navigationView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_preview) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_WP);
            }
            else{
                launchWPServiceAsync async = new launchWPServiceAsync(getApplicationContext(),
                        MainActivity.objectDB);
                async.execute();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_WP:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    launchWPServiceAsync async = new launchWPServiceAsync(getApplicationContext(),
                            MainActivity.objectDB);
                    async.execute();
                }
                break;

            case REQ_VIDEO:
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        // trying to fix the videoview lag
        for(VideoView v : videoViews){
            v.stopPlayback();
        }
        videoViews.clear();

        navigationView.getMenu().findItem(R.id.nav_Home).setChecked(false);
        //navigationView.getMenu().findItem(R.id.nav_Upgrade).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_Foreground).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_Background).setChecked(false);
        //navigationView.getMenu().findItem(R.id.nav_Library).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_Help).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_About).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_Support).setChecked(false);

        resetArrays();

        SharedPreferences sharedPref =  getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (item.getItemId()) {
            case R.id.nav_Home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_HOME);
                editor.apply();
                break;
            /*case R.id.nav_Upgrade:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new UpgradeFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_UPGRADE);
                editor.apply();
                break;*/
            case R.id.nav_Foreground:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ForegroundFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_FOREGROUND);
                editor.apply();
                break;

            case R.id.nav_Background:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BackgroundFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_BACKGROUND);
                editor.apply();
                break;

            /*case R.id.nav_Library:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LibraryFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_LIBRARY);
                editor.apply();
                break;*/

            case R.id.nav_Help:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HelpFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_HELP);
                editor.apply();
                break;

            case R.id.nav_About:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AboutFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_ABOUT);
                editor.apply();
                break;

            case R.id.nav_Support:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SupportFragment()).addToBackStack(null).commit();
                editor.putInt(FRAGMENT_PREFS_KEY, ID_SUPPORT);
                editor.apply();
                break;
        }

        item.setChecked(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkFragment(int id, NavigationView navigationView){

        if(id == ID_HOME){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_Home).setChecked(true);
        }
        else if(id == ID_FOREGROUND){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ForegroundFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_Foreground).setChecked(true);
        }
        else if(id == ID_BACKGROUND){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new BackgroundFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_Background).setChecked(true);
        }
        else if(id == ID_HELP){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HelpFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_Help).setChecked(true);
        }
        else if(id == ID_ABOUT){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new AboutFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_About).setChecked(true);
        }
        else if(id == ID_SUPPORT){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SupportFragment()).addToBackStack(null).commit();
            navigationView.getMenu().findItem(R.id.nav_Support).setChecked(true);
        }
    }

    private void resetArrays(){
        foregroundAdapterObjects = new ArrayList<>();
        backgroundAdapterBackgrounds = new ArrayList<>();
        libraryAdapterImages = new ArrayList<>();
    }

/*    private void startWPTask(Context context){
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, WPService.class));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }*/

    public static class launchWPServiceAsync extends AsyncTask<Void, Void, Void> {

        private Context context;
        private AppDatabase database;

        public launchWPServiceAsync(Context c , AppDatabase db){
            this.context = c;
            this.database = db;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ArrayList<RawObject> rawObjects = (ArrayList<RawObject>) database.objectDao().getAllObjects();
            ArrayList<Background> backgrounds = (ArrayList<Background>) database.backgroundDao().getBackgrounds();

            ArrayList<ForegroundObject> foregroundObjects = new ArrayList<>();

            for (int i = 0; i < rawObjects.size(); i++){
                RawObject r = rawObjects.get(i);
                ForegroundObject obj = new ForegroundObject(context, r.getId(), r.isEnabled(),
                        r.getImageName(), r.isUsesLibraryImage(), r.isUseColor(), r.getColor(), r.isChangeOnBounce(),
                        r.getSize(), r.getSpeed(), r.getAngle(), r.usesGravity(), r.usesShadow(), r.isFlipXonBounce(), r.isFlipYonBounce());
                foregroundObjects.add(obj);
            }
            foregroundAdapterObjects = foregroundObjects;

            if(backgrounds.size() > 0) {
                MainActivity.backgroundAdapterBackgrounds = backgrounds;
            }
            else{
                Background background = new Background(0, Color.argb(0xff, 0x11, 0x11, 0x11));
                database.backgroundDao().insertBackground(background);
                MainActivity.backgroundAdapterBackgrounds.add(background);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Log.d("WP SIZE", WPService.wpObjects.size() + "");

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            //wallpaperManager.forgetLoadedWallpaper();
            if(wallpaperManager.getWallpaperInfo() != null && wallpaperManager.getWallpaperInfo().getPackageName().equals(context.getPackageName())) {
                try {
                    wallpaperManager.clear();
                } catch (Exception e) {
                    Log.d("failure", ":(");
                }
            }


            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, WPService.class));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            /*final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, GLWallpaperService.class));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/

            // manage all wallpapers
            /*Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/

            /*Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, WPService.class));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            context.startActivity(intent);*/

        }

    }



}
