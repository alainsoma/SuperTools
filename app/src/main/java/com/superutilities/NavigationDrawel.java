package com.superutilities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.facebook.FacebookActivity;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.superutilities.BubbleLevel.FragmentBubbleLevel;
import com.superutilities.BubbleLevel.Level;
import com.superutilities.MagnifyingGlass.MagnifierActivity;
import com.superutilities.Ruler.RulerActivity;
import com.superutilities.UnitConverterUltimate.MainConvertionActivity;
import com.superutilities.colorpicker.ColorPickerActivity;
import com.superutilities.Compass.CompassActivity;
import com.superutilities.MetalDetector.MetalDetectorActivity;
import com.superutilities.Mirror.MirrorActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class NavigationDrawel extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentBubbleLevel.OnFragmentInteractionListener,
        View.OnClickListener{

    Button buttonLevel, buttonConvert, buttonGlass, buttonColorPicker, buttonRule, buttonCompass,
            buttonMirror, buttonMetal;

    private AdView mAdView;

    //Agrego los Ads
    private static final long GAME_LENGTH_MILLISECONDS = 3000;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private boolean mGameIsInProgress = false;
    private long mTimerMilliseconds;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        launchInter();
        startGame();

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.left_side_navegation_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_principal);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);


        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.enableAdvertisingIdCollection(true);

        //Log.i(TAG, "Setting screen name: " + "Pantalla Incial");
        mTracker.setScreenName("Abriendo-->" + "Pantalla Incial");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END shared_tracker]

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.ad_view);


        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);



        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buttonLevel = (Button)findViewById(R.id.imageButtonLevel);
        buttonConvert = (Button)findViewById(R.id.imageButtonConverter);
        buttonColorPicker = (Button)findViewById(R.id.buttonColorPicker);
       // buttonRule = (ImageButton)findViewById(R.id.imageButtonRule);
        buttonCompass = (Button)findViewById(R.id.imageButtonCompass);
        //buttonMetal = (ImageButton)findViewById(R.id.imageButtonDetector);

        buttonLevel.setOnClickListener(this);
        buttonConvert.setOnClickListener(this);
        buttonColorPicker.setOnClickListener(this);
        //buttonRule.setOnClickListener(this);
        buttonCompass.setOnClickListener(this);
        //buttonMetal.setOnClickListener(this);
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
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        if (mAdView != null) {
            mAdView.resume();
        }

        if (mGameIsInProgress) {
            resumeGame(mTimerMilliseconds);
        }

        //Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("OnResumen-->" + "Pantalla Principal");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);
        if (mAdView != null) {
            mAdView.pause();
        }
        mCountDownTimer.cancel();
        super.onPause();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navegationdrawel_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        boolean fragmentTransaction = false;
        Fragment fragment = null;

        /*if (id == R.id.nav_acelerometro) {
            startActivity(new Intent(this, Level.class));
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(this, Level.class));
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(this, MagnifierActivity.class));
        } else if (id == R.id.nav_manage) {

        }*/

        if(fragmentTransaction){
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();
            item.setChecked(true);
            getSupportActionBar().setTitle(item.getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.imageButtonLevel) {
            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Share")
                    .setLabel("Level")
                    .build());
            // [END custom_event]
            startActivity(new Intent(this, Level.class));
        } else if (id == R.id.imageButtonConverter){
            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Share")
                    .setLabel("Converter")
                    .build());
            // [END custom_event]
            startActivity(new Intent(this, MainConvertionActivity.class));
        }/* else if (id == R.id.imageButtonGlass) {
            startActivity(new Intent(this, MagnifierActivity.class));
        }*/ else if (id == R.id.buttonColorPicker) {
            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Share")
                    .setLabel("ColorPicker")
                    .build());
            // [END custom_event]
            startActivity(new Intent(this, ColorPickerActivity.class));
        }/* else if (id == R.id.imageButtonRule) {
            startActivity(new Intent(this, RulerActivity.class));
        }*/ else if (id == R.id.imageButtonCompass) {
            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Share")
                    .setLabel("Compass")
                    .build());
            // [END custom_event]
            startActivity(new Intent(this, CompassActivity.class));
        }/* else if (id == R.id.imageButtonMirror) {
            startActivity(new Intent(this, MirrorActivity.class));
        } else if (id == R.id.imageButtonDetector) {
            startActivity(new Intent(this, MetalDetectorActivity.class));
        }*/
    }

    private void createTimer(final long milliseconds) {
        // Create the game timer, which counts down to the end of the level
        // and shows the "retry" button.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(milliseconds, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                mTimerMilliseconds = millisUnitFinished;
            }

            @Override
            public void onFinish() {
                mGameIsInProgress = false;
            }
        };
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            startGame();
        }
    }

    private void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
        resumeGame(GAME_LENGTH_MILLISECONDS);
    }

    private void resumeGame(long milliseconds) {
        // Create a new timer for the correct length and start it.
        mGameIsInProgress = true;
        mTimerMilliseconds = milliseconds;
        createTimer(milliseconds);
        mCountDownTimer.start();
    }

    private void launchInter(){

        //Agrego los Ads
        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_converter_page_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(){
                showInterstitial();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdClosed() {
                if(mGameIsInProgress){
                    mGameIsInProgress=false;
                }
            }
        });

    }
}
