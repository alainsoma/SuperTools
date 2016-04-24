package com.superutilities.colorpicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.shamanland.fab.FloatingActionButton;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.superutilities.AnalyticsApplication;
import com.superutilities.R;

public class ColorPickerActivity extends Activity {

    private static final String APPLICATION_NAME = "colorpicker";

    private static final int CAPTURE_ACTIVITY_REQUEST_CODE = 100;
    private static final int SELECT_ACTIVITY_REQUEST_CODE = 200;

    private static final String KEY_PHOTO_PATH = "photoUri";
    private static final String KEY_COLOR_COMPONENTS = "rgb";
    private static final CharSequence KEY_COLOR_COMPONENTS2 = "rgb2";

    private Uri photoUri;
    private ImageView imageView;
    private com.superutilities.colorpicker.RalColor ralColor = null;
    private com.superutilities.colorpicker.RalColor ralColor2;
    FloatingActionButton fab;

    FloatingActionButton fab2;

    //Agrego los Ads
    private static final long GAME_LENGTH_MILLISECONDS = 3000;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer mCountDownTimer;
    private boolean mGameIsInProgress = false;
    private long mTimerMilliseconds;

    private Tracker mTracker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        setContentView(R.layout.colorpicker_mainview_layout);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Log.i(TAG, "Setting screen name: " + "Pantalla ColorPicker");
        mTracker.setScreenName("Abriendo-->" + "Pantalla ColorPicker");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        final TextView hex =(TextView) findViewById(R.id.textViewHex);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentGallery = new Intent();
                intentGallery.setType("image/*");
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(
                        Intent.createChooser(
                                intentGallery,
                                getString(R.string.select_picture)),
                        SELECT_ACTIVITY_REQUEST_CODE);


            }


        });
        FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hexString = hex.getText().toString();
                //removing "HEX:" from the string returned by getText
                hexString = hexString.replace("HEX:","");
                ClipboardManager _clipboard=
                        (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);


                ClipData clip=
                        ClipData.newPlainText("hex", hexString);
                _clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_clipboard), hex.getText()), Toast.LENGTH_SHORT).show();


            }


        });




        if (imageView == null) {
            imageView = (ImageView)findViewById(R.id.imageView);
            if (imageView != null) {
                ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        if (photoUri != null) {
                            try {
                                showCapturedImage();
                                updateResultData();
                            } catch (FileNotFoundException e) {
                                Log.e(APPLICATION_NAME, "File ".
                                        concat(photoUri.getPath()).concat(" not found!"));
                            } catch (Exception e) {
                                // Ignore
                            }
                        }


                        imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
            }
        }

        if (savedInstanceState != null) {
            String photoUriPath = savedInstanceState.getString(KEY_PHOTO_PATH);
            if (photoUriPath != null) {
                photoUri = Uri.fromFile(new File(photoUriPath));
            }

            if (savedInstanceState.containsKey(KEY_COLOR_COMPONENTS)) {
                ralColor = new com.superutilities.colorpicker.RalColor(
                        savedInstanceState.getInt(KEY_COLOR_COMPONENTS));
            }
        }

        launchInter();
        startGame();
    }

    protected void updateResultData() {
        int index;
        int red = Color.red(ralColor.getColor());
        int green = Color.green(ralColor.getColor());
        int blue = Color.blue(ralColor.getColor());


        index = ralColor.getIndex();
        String[] colorNames = getResources().getStringArray(R.array.color_names);

        TextView textViewColorName = (TextView) findViewById(R.id.textViewColorName);
        try {
            textViewColorName.setText(colorNames[index]);
        } catch (ArrayIndexOutOfBoundsException e) {

            textViewColorName.setText(colorNames[colorNames.length-1]);
        }


        ImageView imageViewColor = (ImageView)findViewById(R.id.imageViewColor);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        imageViewColor.setBackgroundColor(ralColor.getColor());

        TextView textViewRal = (TextView)findViewById(R.id.textViewRal);
        textViewRal.setText(
                "RAL: ".concat(Integer.toString(ralColor.getCode(), 10)));

        TextView textViewRgb = (TextView)findViewById(R.id.textViewRgb);
        textViewRgb.setText(
                "RGB: ".concat(Integer.toString(red , 10)).
                        concat(", ").concat(Integer.toString(green, 10)).
                        concat(", ").concat(Integer.toString(blue, 10)));

        TextView textViewHex = (TextView)findViewById(R.id.textViewHex);
        textViewHex.setText(
                "HEX: #".concat(com.superutilities.colorpicker.Utils.beautyHexString(Integer.toHexString(red))).
                        concat(com.superutilities.colorpicker.Utils.beautyHexString(Integer.toHexString(green))).
                        concat(com.superutilities.colorpicker.Utils.beautyHexString(Integer.toHexString(blue))));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(APPLICATION_NAME, "onResume method entered");

        imageView = (ImageView) findViewById(R.id.imageView);
        if (imageView != null) {
            ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (photoUri != null) {
                        try {
                            showCapturedImage();
                            updateResultData();
                        } catch (FileNotFoundException e) {
                            Log.e(APPLICATION_NAME, "File ".
                                    concat(photoUri.getPath()).concat(" not found!"));
                        } catch (Exception e) {
                            // Ignore
                        }
                    }


                    imageView.setOnTouchListener(onTouchListener);
                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }

        if (mGameIsInProgress) {
            resumeGame(mTimerMilliseconds);
        }

        //Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("OnResumen-->" + "Pantalla ColorPicker");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(APPLICATION_NAME, "onStop method entered");

        mCountDownTimer.cancel();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(APPLICATION_NAME, "onDestroy method entered");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(APPLICATION_NAME, "onSaveInstanceState");

        if (photoUri != null) {
            String realPath;
            try {
                realPath = getRealPathFromURI(photoUri);
            } catch (UnsupportedEncodingException e) {
                realPath = null;
            }
            outState.putString(KEY_PHOTO_PATH, realPath);
        }

        if (ralColor != null) {
            outState.putInt(KEY_COLOR_COMPONENTS, ralColor.getColor());
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {



            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            int action = motionEvent.getAction();
            switch(action) {
                case(MotionEvent.ACTION_DOWN):
                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();
                    int color;


                    try {
                        color = com.superutilities.colorpicker.Utils.findColor(view, x, y);
                    } catch (NullPointerException e) {
                        return false;
                    }


                    if (ralColor == null) {
                        ralColor = new com.superutilities.colorpicker.RalColor(color);
                    } else {
                        ralColor.setColor(color);
                    }

                    updateResultData();
            }
            return false;
        }
    };

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        if (requestCode == CAPTURE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(APPLICATION_NAME, "Capture result OK");
                try {
                    showCapturedImage();
                } catch (FileNotFoundException fileNotFoundException) {

                } catch (NullPointerException nullPointerException) {
                    imageView = (ImageView)findViewById(R.id.imageView);
                    try {
                        showCapturedImage();
                    } catch (Exception exception) {
                        // Do nothing
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, R.string.action_canceled,
                        Toast.LENGTH_SHORT).show();
            } else {

            }
        }

        if (requestCode == SELECT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(APPLICATION_NAME, "Select result OK");
                try {
                    photoUri = data.getData();
                    showCapturedImage();
                } catch (FileNotFoundException e) {

                } catch (NullPointerException e) {
                    imageView = (ImageView)findViewById(R.id.imageView);
                    try {
                        showCapturedImage();
                    } catch (Exception exception) {

                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, R.string.action_canceled,
                        Toast.LENGTH_SHORT).show();
            } else {

            }
        }

        if (resultCode == Activity.RESULT_OK) {
            imageView.setOnTouchListener(onTouchListener);
        }
    }


    private void showCapturedImage() throws
            FileNotFoundException, NullPointerException {

        if (imageView == null) {
            throw new NullPointerException();
        }


        FrameLayout frameLayoutImage = (FrameLayout)findViewById(R.id.frameLayoutImage);
        int targetW = frameLayoutImage.getWidth();
        int targetH = frameLayoutImage.getHeight();


        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                getContentResolver().openInputStream(photoUri),
                null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;


        int scaleFactor = 1;
        try {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        } catch (ArithmeticException arithmeticException) {
            Log.w(APPLICATION_NAME, "frameLayout not yet inflated, no scaling");
        }


        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(photoUri),
                    null, bmOptions);

            int bitmapSize = bitmap.getRowBytes() * bitmap.getHeight();


            if ( bitmapSize > 20000000) {
                bmOptions.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(photoUri),
                        null, bmOptions);
            }

            imageView.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            Log.e(APPLICATION_NAME, e.getLocalizedMessage());
        }

    }


    private static Uri getOutputMediaFileUri(){
        try {
            return Uri.fromFile(getOutputMediaFile());
        } catch(NullPointerException e) {
            return null;
        }
    }


    private static File getOutputMediaFile(){


        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), APPLICATION_NAME);


        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e(APPLICATION_NAME, "failed to create directory");
                return null;
            }
        }

        // Create a media file pseudo random name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).
                format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }


    @SuppressWarnings("deprecation")
    private String getRealPathFromURI(Uri contentUri) throws UnsupportedEncodingException {
        String realPath;

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            realPath = contentUri.toString();
        } else {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            realPath = cursor.getString(column_index);
        }


        if (realPath.startsWith("file://")) {
            realPath = realPath.substring("file://".length());
        }
        return URLDecoder.decode(realPath, "UTF-8");
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
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_colorpicker_page_id));

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

    @Override
    public void onPause() {
        // Cancel the timer if the game is paused.
        mCountDownTimer.cancel();
        super.onPause();
    }


}
