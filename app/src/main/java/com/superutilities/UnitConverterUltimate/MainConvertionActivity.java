/*
 * Copyright 2015 Phil Shadlyn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.superutilities.UnitConverterUltimate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.superutilities.AnalyticsApplication;
import com.superutilities.R;
import com.superutilities.UnitConverterUltimate.fragments.ConversionFragment;
import com.superutilities.UnitConverterUltimate.models.Conversion;
import com.superutilities.UnitConverterUltimate.util.Conversions;

/**
 * Main activity
 * Created by Phizz on 15-07-28.
 */
public class MainConvertionActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private Conversions mConversions;

    //Agrego los Ads
    private static final long GAME_LENGTH_MILLISECONDS = 3000;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private boolean mGameIsInProgress = false;
    private long mTimerMilliseconds;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        launchInter();
        startGame();

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Log.i(TAG, "Setting screen name: " + "Pantalla Converter");
        mTracker.setScreenName("Image~" + "Pantalla Converter");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Preferences.getInstance(this).getPreferences().registerOnSharedPreferenceChangeListener(this);
        mConversions = Conversions.getInstance();

        setContentView(R.layout.converter_activity_main);
        setupToolbar();
        setToolbarHomeNavigation(true);



        if(getSupportActionBar() != null) getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);


        int conversion = Preferences.getInstance(this).getLastConversion();
        setToolbarTitle(mConversions.getById(conversion).getLabelResource());
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //setupDrawer(conversion);

        if(savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ConversionFragment.newInstance(conversion))
                    .commit();
        }



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

    @Override
    protected void onStart()
    {
        super.onStart();

        // Show help dialog if never seen before
        /*if(Preferences.getInstance(this).showHelp())
        {
            HelpDialogFragment.newInstance().show(getSupportFragmentManager(), HelpDialogFragment.TAG);
        }*/
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Preferences.getInstance(this).getPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(Preferences.PREFS_THEME))
        {
            recreate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        // Start or resume the game.
        super.onResume();

        if (mGameIsInProgress) {
            resumeGame(mTimerMilliseconds);
        }
    }

    @Override
    public void onPause() {
        // Cancel the timer if the game is paused.
        mCountDownTimer.cancel();
        super.onPause();
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
                    mGameIsInProgress = false;
                }
            }
        });

    }
}