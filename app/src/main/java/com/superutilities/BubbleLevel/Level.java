package com.superutilities.BubbleLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.superutilities.AnalyticsApplication;
import com.superutilities.BubbleLevel.orientation.Orientation;
import com.superutilities.BubbleLevel.orientation.OrientationListener;
import com.superutilities.BubbleLevel.orientation.OrientationProvider;
import com.superutilities.BubbleLevel.view.LevelView;
import com.superutilities.R;


public class Level extends Activity implements OrientationListener {
	
	private static Level CONTEXT;
	
	private static final int DIALOG_CALIBRATE_ID = 1;
	private static final int TOAST_DURATION = 10000;
	
	private OrientationProvider provider;
	
    private LevelView view;

	/** Gestion du son */
	private SoundPool soundPool;
	private boolean soundEnabled;
	private int bipSoundID;
	private int bipRate;
	private long lastBip;

	//Agrego los Ads
	private static final long GAME_LENGTH_MILLISECONDS = 3000;
	private InterstitialAd mInterstitialAd;
	private CountDownTimer mCountDownTimer;
	private boolean mGameIsInProgress = false;
	private long mTimerMilliseconds;

	private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bubblelevel_levelview_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CONTEXT = this;
        view = (LevelView) findViewById(R.id.level);
        // sound
    	soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
    	bipSoundID = soundPool.load(this, R.raw.bip, 1);
    	bipRate = getResources().getInteger(R.integer.bip_rate);

		// [START shared_tracker]
		// Obtain the shared Tracker instance.
		AnalyticsApplication application = (AnalyticsApplication) getApplication();
		mTracker = application.getDefaultTracker();

		//Log.i(TAG, "Setting screen name: " + "Pantalla Level");
		mTracker.setScreenName("Abriendo-->" + "Pantalla Level");
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());

		launchInter();
		startGame();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bubblelevel_calibrate_settings_menu, menu);
	    return true;
	}
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.calibrate:
	            showDialog(DIALOG_CALIBRATE_ID);
	            return true;
	        case R.id.preferences:
	            startActivity(new Intent(this, LevelPreferences.class));
	            return true;
        }
        return false;
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
	        case DIALOG_CALIBRATE_ID:
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.calibrate_title)
	        			.setIcon(null)
	        			.setCancelable(true)
	        			.setPositiveButton(R.string.calibrate, new DialogInterface.OnClickListener() {
	        	           	public void onClick(DialogInterface dialog, int id) {
	        	        	   	provider.saveCalibration();
	        	           	}
	        			})
	        	       	.setNegativeButton(R.string.cancel, null)
	        	       	.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
	        	           	public void onClick(DialogInterface dialog, int id) {
	        	           		provider.resetCalibration();
	        	           	}
	        	       	})
	        	       	.setMessage(R.string.calibrate_message);
	        	dialog = builder.create();
	            break;
	        default:
	            dialog = null;
        }
        return dialog;
    }
    
    protected void onResume() {
    	super.onResume();
    	Log.d("Level", "Level resumed");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	provider = OrientationProvider.getInstance();
    	// chargement des effets sonores
        soundEnabled = prefs.getBoolean(LevelPreferences.KEY_SOUND, false);
        // orientation manager
        if (provider.isSupported()) {
    		provider.startListening(this);
    	} else {
    		Toast.makeText(this, getText(R.string.not_supported), Toast.LENGTH_LONG).show();
    	}

		if (mGameIsInProgress) {
			resumeGame(mTimerMilliseconds);
		}

		//Log.i(TAG, "Setting screen name: " + name);
		mTracker.setScreenName("OnResumen-->" + "Pantalla Level");
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (provider.isListening()) {
        	provider.stopListening();
    	}
		mCountDownTimer.cancel();
		super.onPause();
    }
    
    @Override
    public void onDestroy() {
		if (soundPool != null) {
			soundPool.release();
		}
		super.onDestroy();
    }

	@Override
	public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance) {
	    if (soundEnabled 
				&& orientation.isLevel(pitch, roll, balance, provider.getSensibility())
				&& System.currentTimeMillis() - lastBip > bipRate) {
			AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_RING);
			float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_RING);
			float volume = streamVolumeCurrent / streamVolumeMax;
			lastBip = System.currentTimeMillis();
			soundPool.play(bipSoundID, volume, volume, 1, 0, 1);
		}
		view.onOrientationChanged(orientation, pitch, roll, balance);
	}

	@Override
	public void onCalibrationReset(boolean success) {
		Toast.makeText(this, success ? 
				R.string.calibrate_restored : R.string.calibrate_failed,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCalibrationSaved(boolean success) {
		Toast.makeText(this, success ? 
				R.string.calibrate_saved : R.string.calibrate_failed,
				Toast.LENGTH_LONG).show();
	}

    public static Level getContext() {
		return CONTEXT;
	}
    
    public static OrientationProvider getProvider() {
    	return getContext().provider;
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
		mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_level_page_id));

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
