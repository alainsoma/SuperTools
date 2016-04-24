package com.superutilities.BubbleLevel.view;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.superutilities.BubbleLevel.LevelPreferences;
import com.superutilities.BubbleLevel.config.DisplayType;
import com.superutilities.BubbleLevel.config.Viscosity;
import com.superutilities.BubbleLevel.painter.LevelPainter;
import com.superutilities.BubbleLevel.orientation.*;

public class LevelView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {

	private LevelPainter painter;

    public LevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
    	setOnTouchListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (painter != null) {
        	painter.pause(!hasWindowFocus);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	if (painter != null) {
    		painter.setSurfaceSize(width, height);
    	}
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    	if (painter == null) {
    		painter = new LevelPainter(holder, getContext(), new Handler(), getWidth(), getHeight(), 
				prefs.getBoolean(LevelPreferences.KEY_SHOW_ANGLE, true),
				DisplayType.valueOf(prefs.getString(LevelPreferences.KEY_DISPLAY_TYPE, "ANGLE")),
				Viscosity.valueOf(prefs.getString(LevelPreferences.KEY_VISCOSITY, "MEDIUM")),
				prefs.getBoolean(LevelPreferences.KEY_LOCK, false),
				prefs.getBoolean(LevelPreferences.KEY_ECONOMY, false));
	    }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (painter != null) {
        	painter.pause(true);
        	painter.clean();
	        painter = null;
        }
        // free resources
        System.gc();
    }

    public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance) {
		if (painter != null) {
			painter.onOrientationChanged(orientation, pitch, roll, balance);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && painter != null) {
			painter.onTouch((int) event.getX(), (int) event.getY());
		}
		return true;
	}

}
